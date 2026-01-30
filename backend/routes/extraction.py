from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from pydantic import BaseModel, Field
from typing import List, Optional, Dict, Any
import json
import os

from ..database import get_db
from ..models import Story, Character, Location, TimelineEvent, LoreEntry, Manuscript

router = APIRouter(prefix="/api/extraction", tags=["extraction"])

# Configuration LLM
LLM_PROVIDER = os.getenv("LLM_PROVIDER", "anthropic")
ANTHROPIC_API_KEY = os.getenv("ANTHROPIC_API_KEY", "")
OPENAI_API_KEY = os.getenv("OPENAI_API_KEY", "")


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
    """Appelle le LLM configuré"""
    if LLM_PROVIDER == "anthropic":
        if not ANTHROPIC_API_KEY:
            raise HTTPException(503, "ANTHROPIC_API_KEY non configurée")

        try:
            import anthropic
        except ImportError:
            raise HTTPException(503, "Module 'anthropic' non installé")

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
Tu dois identifier précisément les personnages, lieux, événements chronologiques et éléments de lore (world-building).
Réponds UNIQUEMENT en JSON valide, sans texte supplémentaire."""

    # Construire le prompt utilisateur
    extract_instructions = []

    if "characters" in request.extract_types:
        extract_instructions.append("""
**PERSONNAGES** : Identifie tous les personnages mentionnés avec :
- name (prénom ou nom complet)
- surname (nom de famille si différent)
- role (protagoniste, antagoniste, secondaire, etc.)
- age (si mentionné ou déductible)
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
      "age": null,
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

            char = Character(
                story_id=request.story_id,
                name=request.item_data.get("name"),
                surname=request.item_data.get("surname"),
                role=request.item_data.get("role"),
                age=request.item_data.get("age"),
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
            event = TimelineEvent(
                story_id=request.story_id,
                title=request.item_data.get("title"),
                date=request.item_data.get("date"),
                sort_order=request.item_data.get("sort_order", 0),
                summary=request.item_data.get("summary")
            )

            # Associer le lieu si mentionné
            if request.item_data.get("location_name"):
                loc = db.query(Location).filter(
                    Location.story_id == request.story_id,
                    Location.name == request.item_data.get("location_name")
                ).first()
                if loc:
                    event.location_id = loc.id

            db.add(event)
            db.flush()

            # Associer les personnages si mentionnés
            if request.item_data.get("character_names"):
                for char_name in request.item_data["character_names"]:
                    char = db.query(Character).filter(
                        Character.story_id == request.story_id,
                        Character.name.ilike(f"%{char_name}%")
                    ).first()
                    if char:
                        event.characters.append(char)

            db.commit()
            db.refresh(event)
            return {"status": "created", "item_type": "timeline", "id": event.id, "data": event.to_dict()}

        # LORE
        elif request.item_type == "lore":
            existing = db.query(LoreEntry).filter(
                LoreEntry.story_id == request.story_id,
                LoreEntry.title == request.item_data.get("title")
            ).first()

            if existing:
                return {"status": "duplicate", "message": f"L'entrée de lore '{request.item_data.get('title')}' existe déjà"}

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
        raise HTTPException(500, f"Erreur lors de la création: {str(e)}")


@router.post("/batch-validate")
async def batch_validate_and_create(
        story_id: int,
        items: List[ValidationRequest],
        db: Session = Depends(get_db)
):
    """
    Valide et crée plusieurs éléments en une seule fois.
    Utile pour valider tous les personnages d'un coup, par exemple.
    """
    results = []

    for item in items:
        if item.approved:
            try:
                result = await validate_and_create(item, db)
                results.append(result)
            except Exception as e:
                results.append({
                    "status": "error",
                    "item_type": item.item_type,
                    "message": str(e)
                })

    return {
        "total": len(items),
        "approved": len([r for r in results if r.get("status") == "created"]),
        "rejected": len([i for i in items if not i.approved]),
        "duplicates": len([r for r in results if r.get("status") == "duplicate"]),
        "errors": len([r for r in results if r.get("status") == "error"]),
        "results": results
    }