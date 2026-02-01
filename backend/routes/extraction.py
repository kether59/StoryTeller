import json
import logging
import os
import re
from typing import List, Optional, Dict, Any

from fastapi import APIRouter, Depends, HTTPException
from pydantic import BaseModel, Field, field_validator
from sqlalchemy.orm import Session

from ..database import get_db
from ..models import Character, Location, TimelineEvent, LoreEntry, Manuscript

router = APIRouter(prefix="/api/extraction", tags=["extraction"])

logger = logging.getLogger(__name__)

# Configuration LLM
LLM_PROVIDER = os.getenv("LLM_PROVIDER", "anthropic")
ANTHROPIC_API_KEY = os.getenv("ANTHROPIC_API_KEY", "")
OPENAI_API_KEY = os.getenv("OPENAI_API_KEY", "")
OPENROUTER_API_KEY = os.getenv("OPENROUTER_API_KEY", "")
OLLAMA_URL = os.getenv("OLLAMA_URL", "http://localhost:11434")



# ===== Models Pydantic =====

class ExtractionRequest(BaseModel):
    manuscript_id: int
    extract_types: List[str] = Field(
        default=["characters", "locations", "timeline", "lore"],
        description="Types d'éléments à extraire"
    )


class ExtractedCharacter(BaseModel):
    name: str
    surname: Optional[str] = None
    role: Optional[str] = None
    age: Optional[int] = None
    physical_description: Optional[str] = None
    personality: Optional[str] = None
    motivation: Optional[str] = None
    confidence: float = Field(default=0.0, description="Niveau de confiance de l'extraction (0-1)")

    @field_validator('age', mode='before')
    @classmethod
    def parse_age(cls, v):
        """Convertir l'âge depuis string vers int
        Le LLM peut retourner "30 ans", "environ 25", etc.
        Cette fonction extrait le premier nombre trouvé.
        """
        if v is None or v == "":
            return None
        if isinstance(v, int):
            return v
        if isinstance(v, str):
            # Extraire les chiffres de la chaîne (ex: "30 ans" -> 30)
            digits = re.findall(r'\d+', v)
            return int(digits[0]) if digits else None
        return None

class ExtractedLocation(BaseModel):
    name: str
    type: Optional[str] = None
    summary: Optional[str] = None
    confidence: float = 0.0


class ExtractedTimelineEvent(BaseModel):
    title: str
    date: Optional[str] = None
    summary: Optional[str] = None
    sort_order: int = 0
    character_names: List[str] = []
    location_name: Optional[str] = None
    confidence: float = 0.0


class ExtractedLore(BaseModel):
    title: str
    category: Optional[str] = None
    content: Optional[str] = None
    confidence: float = 0.0


class ExtractionResult(BaseModel):
    characters: List[ExtractedCharacter] = []
    locations: List[ExtractedLocation] = []
    timeline: List[ExtractedTimelineEvent] = []
    lore: List[ExtractedLore] = []
    raw_response: Optional[str] = None


class ValidationRequest(BaseModel):
    story_id: int
    item_type: str  # "character", "location", "timeline", "lore"
    item_data: Dict[str, Any]
    approved: bool


# ===== Helper Functions =====

async def call_llm(system_prompt: str, user_prompt: str, max_tokens: int = 4000) -> str:
    """
    Appelle le LLM configuré et retourne la réponse.
    Supporte: Anthropic, OpenAI, OpenRouter, Ollama
    """
    if LLM_PROVIDER == "anthropic":
        if not ANTHROPIC_API_KEY:
            raise HTTPException(503, "ANTHROPIC_API_KEY non configurée")

        try:
            import anthropic
        except ImportError:
            raise HTTPException(503, "Module 'anthropic' non installé. Exécutez: pip install anthropic")

        client = anthropic.Anthropic(api_key=ANTHROPIC_API_KEY)
        message = client.messages.create(
            model="claude-sonnet-4-20250514",
            max_tokens=max_tokens,
            system=system_prompt,
            messages=[{"role": "user", "content": user_prompt}]
        )
        return message.content[0].text

    elif LLM_PROVIDER == "openai":
        if not OPENAI_API_KEY:
            raise HTTPException(503, "OPENAI_API_KEY non configurée")

        try:
            import openai
        except ImportError:
            raise HTTPException(503, "Module 'openai' non installé")

        client = openai.OpenAI(api_key=OPENAI_API_KEY)
        response = client.chat.completions.create(
            model="gpt-4-turbo-preview",
            messages=[
                {"role": "system", "content": system_prompt},
                {"role": "user", "content": user_prompt}
            ],
            max_tokens=max_tokens
        )
        return response.choices[0].message.content

    elif LLM_PROVIDER == "openrouter":
        if not OPENROUTER_API_KEY:
            raise HTTPException(503, "OPENROUTER_API_KEY non configurée")

        try:
            import openai
        except ImportError:
            raise HTTPException(503, "Module 'openai' non installé")

        client = openai.OpenAI(
            base_url="https://openrouter.ai/api/v1",
            api_key=OPENROUTER_API_KEY,
        )

        model = os.getenv("OPENROUTER_MODEL", "meta-llama/llama-3.1-8b-instruct:free")

        # LOGS : Visualiser l'envoi
        print(f"--- APPEL OPENROUTER ---")
        print(f"Modèle: {model}")

        response = client.chat.completions.create(
            model=model,
            messages=[
                {"role": "system", "content": system_prompt},
                {"role": "user", "content": user_prompt}
            ],
            max_tokens=max_tokens
        )

        # CORRECTION ICI : Accès correct au contenu
        content = response.choices[0].message.content

        # LOGS : Voir la réponse brute
        print(f"Réponse reçue (tronquée): {content[:100]}...")

        return content

    elif LLM_PROVIDER == "ollama":
        try:
            import httpx
        except ImportError:
            raise HTTPException(503, "Module 'httpx' non installé. Exécutez: pip install httpx")

        import asyncio

        async def call_ollama_async():
            async with httpx.AsyncClient(timeout=300.0) as client:
                response = await client.post(
                    f"{OLLAMA_URL}/api/generate",
                    json={
                        "model": "mistral",  # You can make this configurable
                        "prompt": f"{system_prompt}\n\nUser: {user_prompt}\n\nAssistant:",
                        "stream": False
                    }
                )

                if response.status_code != 200:
                    raise HTTPException(503, f"Ollama error: {response.text}")

                return response.json()["response"]

        # Run async function in sync context
        loop = asyncio.get_event_loop()
        if loop.is_running():
            # If we're already in an async context, just await
            import nest_asyncio
            nest_asyncio.apply()

        return asyncio.run(call_ollama_async())

    else:
        raise HTTPException(400, f"LLM provider '{LLM_PROVIDER}' non supporté")


def clean_json_response(text: str) -> str:
    """Nettoie la réponse pour extraire le JSON"""
    text = text.strip()

    # Enlever les backticks markdown
    if text.startswith("```json"):
        text = text[7:]
    elif text.startswith("```"):
        text = text[3:]

    if text.endswith("```"):
        text = text[:-3]

    return text.strip()


# ===== Routes =====

@router.post("/analyze", response_model=ExtractionResult)
async def analyze_manuscript(request: ExtractionRequest, db: Session = Depends(get_db)):
    """
    Analyse un manuscrit et extrait les personnages, lieux, chronologie et lore.
    Retourne les données structurées pour validation avant insertion.
    """

    # Récupérer le manuscrit
    manuscript = db.query(Manuscript).filter(Manuscript.id == request.manuscript_id).first()
    if not manuscript:
        raise HTTPException(404, "Manuscrit introuvable")

    if not manuscript.text or len(manuscript.text) < 100:
        raise HTTPException(400, "Le manuscrit est trop court pour être analysé")

    # Construire le prompt système
    system_prompt = """Tu es un assistant expert en analyse narrative. 
Tu extrais des informations structurées depuis des textes littéraires.
Tu dois identifier précisément les personnages, lieux, événements chronologiques et éléments de lore.

IMPORTANT : 
1. Tu dois rédiger TOUTES les descriptions et contenus en FRANÇAIS.
2. Même si le format est en JSON, les valeurs textuelles doivent être en français littéraire.
3. Réponds UNIQUEMENT en JSON valide, sans texte supplémentaire."""

    # Construire le prompt utilisateur
    extract_instructions = []

    if "characters" in request.extract_types:
        extract_instructions.append("""
**PERSONNAGES** : Identifie tous les personnages mentionnés avec :
- name (prénom ou nom complet)
- surname (nom de famille si différent)
- role (protagoniste, antagoniste, secondaire, etc.)
- age (NOMBRE ENTIER uniquement, par exemple: 25, pas "25 ans" ni "environ 25")
- physical_description (apparence physique)
- personality (traits de caractère observés)
- motivation (ce qui les pousse à agir)
- confidence (0.0 à 1.0 : ton niveau de certitude sur ces infos)
""")

    if "locations" in request.extract_types:
        extract_instructions.append("""
**LIEUX** : Liste tous les lieux mentionnés avec :
- name (nom du lieu)
- type (ville, planète, bâtiment, région, etc.)
- summary (description et importance)
- confidence
""")

    if "timeline" in request.extract_types:
        extract_instructions.append("""
**CHRONOLOGIE** : Identifie les événements clés avec :
- title (titre de l'événement)
- date (si mentionnée explicitement)
- summary (résumé de l'événement)
- sort_order (ordre chronologique : 1, 2, 3...)
- character_names (liste des noms de personnages impliqués)
- location_name (nom du lieu où se déroule l'événement)
- confidence
""")

    if "lore" in request.extract_types:
        extract_instructions.append("""
**LORE / WORLD-BUILDING** : Extrait les éléments du monde avec :
- title (nom du concept)
- category (magie, technologie, faction, histoire, culture, etc.)
- content (description détaillée)
- confidence
""")

    user_prompt = f"""Analyse ce texte et extrait les informations demandées.

TEXTE À ANALYSER :
---
{manuscript.text[:8000]}  # Limiter à 8000 caractères pour éviter les timeouts
---

INSTRUCTIONS D'EXTRACTION :
{''.join(extract_instructions)}

IMPORTANT :
- Tout le contenu textuel (descriptions, rôles, résumés) doit impérativement être en FRANÇAIS.
- Pour l'âge, utilise UNIQUEMENT un nombre entier (par exemple: 25), PAS "25 ans" ou "environ 25"
- Sois précis et factuel
- N'invente pas d'informations qui ne sont pas dans le texte
- Si tu n'es pas sûr d'une information, mets confidence à 0.5 ou moins
- Retourne un JSON avec cette structure exacte :

{{
  "characters": [
    {{
      "name": "...",
      "surname": "...",
      "role": "...",
      "age": 25,
      "physical_description": "...",
      "personality": "...",
      "motivation": "...",
      "confidence": 0.9
    }}
  ],
  "locations": [
    {{
      "name": "...",
      "type": "...",
      "summary": "...",
      "confidence": 0.8
    }}
  ],
  "timeline": [
    {{
      "title": "...",
      "date": "...",
      "summary": "...",
      "sort_order": 1,
      "character_names": ["...", "..."],
      "location_name": "...",
      "confidence": 0.7
    }}
  ],
  "lore": [
    {{
      "title": "...",
      "category": "...",
      "content": "...",
      "confidence": 0.85
    }}
  ]
}}

Réponds UNIQUEMENT avec le JSON, rien d'autre.
"""

    try:
        # Appeler le LLM
        raw_response = await call_llm(system_prompt, user_prompt, max_tokens=4000)

        # Nettoyer et parser le JSON
        cleaned = clean_json_response(raw_response)

        try:
            data = json.loads(cleaned)
        except json.JSONDecodeError as e:
            # Si le parsing échoue, retourner la réponse brute
            logger.error(f"Erreur de parsing JSON: {e}")
            logger.error(f"Réponse brute: {cleaned[:500]}")
            return ExtractionResult(raw_response=raw_response)

        # Construire le résultat structuré
        result = ExtractionResult(
            characters=[ExtractedCharacter(**char) for char in data.get("characters", [])],
            locations=[ExtractedLocation(**loc) for loc in data.get("locations", [])],
            timeline=[ExtractedTimelineEvent(**evt) for evt in data.get("timeline", [])],
            lore=[ExtractedLore(**lore) for lore in data.get("lore", [])],
            raw_response=raw_response
        )

        return result

    except Exception as e:
        logger.error(f"Erreur lors de l'extraction: {str(e)}")
        raise HTTPException(500, f"Erreur lors de l'extraction: {str(e)}")


@router.post("/validate-and-create")
async def validate_and_create(request: ValidationRequest, db: Session = Depends(get_db)):
    """
    Valide et crée une entrée dans la base de données.
    Utilisé après que l'utilisateur a approuvé un élément extrait.
    """

    if not request.approved:
        return {"status": "rejected", "message": "Élément rejeté par l'utilisateur"}

    try:
        # PERSONNAGE
        if request.item_type == "character":
            # Vérifier si le personnage existe déjà
            existing = db.query(Character).filter(
                Character.story_id == request.story_id,
                Character.name == request.item_data.get("name")
            ).first()

            if existing:
                return {"status": "duplicate", "message": f"Le personnage '{request.item_data.get('name')}' existe déjà"}

            # Nettoyer l'âge (au cas où une chaîne serait passée)
            raw_age = request.item_data.get("age")
            clean_age = None
            if raw_age:
                if isinstance(raw_age, int):
                    clean_age = raw_age
                elif isinstance(raw_age, str):
                    digits = re.findall(r'\d+', raw_age)
                    clean_age = int(digits[0]) if digits else None

            char = Character(
                story_id=request.story_id,
                name=request.item_data.get("name"),
                surname=request.item_data.get("surname"),
                role=request.item_data.get("role"),
                age=clean_age,
                physical_description=request.item_data.get("physical_description"),
                personality=request.item_data.get("personality"),
                motivation=request.item_data.get("motivation"),
                goal=request.item_data.get("goal"),
                flaw=request.item_data.get("flaw"),
                character_arc=request.item_data.get("character_arc"),
                skills=request.item_data.get("skills"),
                notes=f"Extrait automatiquement (confiance: {request.item_data.get('confidence', 0):.2f})"
            )
            db.add(char)
            db.commit()
            db.refresh(char)
            return {"status": "created", "item_type": "character", "id": char.id, "data": char.to_dict()}

        # LIEU
        elif request.item_type == "location":
            existing = db.query(Location).filter(
                Location.story_id == request.story_id,
                Location.name == request.item_data.get("name")
            ).first()

            if existing:
                return {"status": "duplicate", "message": f"Le lieu '{request.item_data.get('name')}' existe déjà"}

            loc = Location(
                story_id=request.story_id,
                name=request.item_data.get("name"),
                type=request.item_data.get("type"),
                summary=request.item_data.get("summary")
            )
            db.add(loc)
            db.commit()
            db.refresh(loc)
            return {"status": "created", "item_type": "location", "id": loc.id, "data": loc.to_dict()}

        # ÉVÉNEMENT CHRONOLOGIQUE
        elif request.item_type == "timeline":
            evt = TimelineEvent(
                story_id=request.story_id,
                title=request.item_data.get("title"),
                date=request.item_data.get("date"),
                summary=request.item_data.get("summary"),
                sort_order=request.item_data.get("sort_order", 0)
            )
            db.add(evt)
            db.commit()
            db.refresh(evt)

            # TODO: Associer les personnages et lieux si nécessaire
            # Nécessite de récupérer les IDs depuis les noms

            return {"status": "created", "item_type": "timeline", "id": evt.id, "data": evt.to_dict()}

        # LORE
        elif request.item_type == "lore":
            existing = db.query(LoreEntry).filter(
                LoreEntry.story_id == request.story_id,
                LoreEntry.title == request.item_data.get("title")
            ).first()

            if existing:
                return {"status": "duplicate", "message": f"L'entrée lore '{request.item_data.get('title')}' existe déjà"}

            lore = LoreEntry(
                story_id=request.story_id,
                title=request.item_data.get("title"),
                category=request.item_data.get("category"),
                content=request.item_data.get("content")
            )
            db.add(lore)
            db.commit()
            db.refresh(lore)
            return {"status": "created", "item_type": "lore", "id": lore.id, "data": lore.to_dict()}

        else:
            raise HTTPException(400, f"Type d'élément '{request.item_type}' non supporté")

    except Exception as e:
        db.rollback()
        logger.error(f"Erreur lors de la création: {str(e)}")
        raise HTTPException(500, f"Erreur lors de la création: {str(e)}")


@router.get("/health")
async def health_check():
    """Vérifie que le LLM est configuré correctement"""
    return {
        "provider": LLM_PROVIDER,
        "configured": bool(
            (LLM_PROVIDER == "anthropic" and ANTHROPIC_API_KEY) or
            (LLM_PROVIDER == "openai" and OPENAI_API_KEY) or
            (LLM_PROVIDER == "openrouter" and OPENROUTER_API_KEY) or
            (LLM_PROVIDER == "ollama")
        )
    }