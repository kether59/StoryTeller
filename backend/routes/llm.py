from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from pydantic import BaseModel, Field
from typing import Optional, List, Literal
from dotenv import load_dotenv
import os
import json

from ..database import get_db
from ..models import Story, Character, Location, TimelineEvent, LoreEntry, Manuscript

router = APIRouter(prefix="/api/llm", tags=["llm"])

load_dotenv()

# ===== Configuration =====
# Vous pouvez utiliser différents LLMs
LLM_PROVIDER = os.getenv("LLM_PROVIDER", "anthropic")  # anthropic, openai, openrouter, ollama
ANTHROPIC_API_KEY = os.getenv("ANTHROPIC_API_KEY", "")
OPENAI_API_KEY = os.getenv("OPENAI_API_KEY", "")
OPENROUTER_API_KEY = os.getenv("OPENROUTER_API_KEY", "")
OLLAMA_URL = os.getenv("OLLAMA_URL", "http://localhost:11434")


# ===== Models Pydantic =====
class ChapterGenerationRequest(BaseModel):
    story_id: int
    chapter_number: Optional[int] = None
    chapter_title: Optional[str] = None
    summary: str = Field(..., description="Résumé de ce qui doit se passer dans le chapitre")
    style: Optional[str] = Field("narratif", description="Style d'écriture: narratif, dialogue, descriptif, action")
    length: Optional[Literal["court", "moyen", "long"]] = Field("moyen", description="Longueur souhaitée")
    include_characters: Optional[List[int]] = Field(default_factory=list, description="IDs des personnages à inclure")
    include_locations: Optional[List[int]] = Field(default_factory=list, description="IDs des lieux à utiliser")
    tone: Optional[str] = Field("neutre", description="Ton: neutre, dramatique, humoristique, sombre, léger")
    pov: Optional[str] = Field("troisième personne", description="Point de vue narratif")


class ContinueWritingRequest(BaseModel):
    manuscript_id: int
    direction: str = Field(..., description="Direction pour continuer: ex. 'Le héros découvre un secret'")
    length: Optional[int] = Field(500, description="Nombre de mots approximatif")


class RewriteRequest(BaseModel):
    text: str = Field(..., description="Texte à réécrire")
    instruction: str = Field(..., description="Instructions: ex. 'Rendre plus descriptif', 'Ajouter des dialogues'")


class SuggestNextSceneRequest(BaseModel):
    story_id: int
    current_situation: str = Field(..., description="Situation actuelle dans l'histoire")


# ===== Helper Functions =====

def get_story_context(story_id: int, db: Session) -> dict:
    """Récupère tout le contexte de l'histoire pour le LLM"""
    story = db.query(Story).filter(Story.id == story_id).first()
    if not story:
        raise HTTPException(404, "Histoire introuvable")

    characters = db.query(Character).filter(Character.story_id == story_id).all()
    locations = db.query(Location).filter(Location.story_id == story_id).all()
    timeline = db.query(TimelineEvent).filter(TimelineEvent.story_id == story_id).order_by(TimelineEvent.sort_order).all()
    lore = db.query(LoreEntry).filter(LoreEntry.story_id == story_id).all()

    return {
        "story": story,
        "characters": characters,
        "locations": locations,
        "timeline": timeline,
        "lore": lore
    }


def build_system_prompt(context: dict) -> str:
    """Construit le prompt système avec tout le contexte"""
    story = context["story"]

    prompt = f"""Tu es un assistant d'écriture créative expert. Tu aides à rédiger un roman intitulé "{story.title}".

## Synopsis de l'histoire
{story.synopsis or 'Non défini'}

## Personnages principaux
"""

    for char in context["characters"]:
        prompt += f"\n### {char.name} {char.surname or ''}"
        if char.role:
            prompt += f"\n- Rôle: {char.role}"
        if char.age:
            prompt += f"\n- Âge: {char.age} ans"
        if char.personality:
            prompt += f"\n- Personnalité: {char.personality}"
        if char.motivation:
            prompt += f"\n- Motivation: {char.motivation}"
        prompt += "\n"

    if context["locations"]:
        prompt += "\n## Lieux importants\n"
        for loc in context["locations"]:
            prompt += f"\n### {loc.name}"
            if loc.type:
                prompt += f" ({loc.type})"
            if loc.summary:
                prompt += f"\n{loc.summary}"
            prompt += "\n"

    if context["lore"]:
        prompt += "\n## Éléments du monde (Lore)\n"
        for lore in context["lore"]:
            prompt += f"\n### {lore.title}"
            if lore.category:
                prompt += f" - {lore.category}"
            if lore.content:
                prompt += f"\n{lore.content}"
            prompt += "\n"

    if context["timeline"]:
        prompt += "\n## Chronologie des événements\n"
        for event in context["timeline"][:10]:  # Limiter aux 10 premiers
            prompt += f"\n- {event.title}"
            if event.date:
                prompt += f" ({event.date})"
            if event.summary:
                prompt += f": {event.summary}"

    prompt += """

## Instructions
- Respecte la personnalité et les motivations des personnages
- Utilise les éléments du lore de manière cohérente
- Maintiens le ton et le style de l'univers
- Écris en français avec un style littéraire de qualité
- Crée des scènes vivantes avec descriptions et dialogues naturels
"""

    return prompt


async def call_anthropic(system_prompt: str, user_prompt: str, max_tokens: int = 2000) -> str:
    """Appelle l'API Claude d'Anthropic"""
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
        messages=[
            {"role": "user", "content": user_prompt}
        ]
    )

    return message.content[0].text


async def call_openai(system_prompt: str, user_prompt: str, max_tokens: int = 2000) -> str:
    """Appelle l'API OpenAI"""
    if not OPENAI_API_KEY:
        raise HTTPException(503, "OPENAI_API_KEY non configurée")

    try:
        import openai
    except ImportError:
        raise HTTPException(503, "Module 'openai' non installé. Exécutez: pip install openai")

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


async def call_openrouter(system_prompt: str, user_prompt: str, max_tokens: int = 2000) -> str:
    """Appelle l'API OpenRouter"""
    if not OPENROUTER_API_KEY:
        raise HTTPException(503, "OPENROUTER_API_KEY non configurée")

    try:
        import openai
    except ImportError:
        raise HTTPException(503, "Module 'openai' non installé. Exécutez: pip install openai")

    # OpenRouter utilise une API compatible OpenAI
    client = openai.OpenAI(
        base_url="https://openrouter.ai/api/v1",
        api_key=OPENROUTER_API_KEY,
    )

    # Choix du modèle (configurable via variable d'environnement)
    # Modèles GRATUITS:
    #   - meta-llama/llama-3.1-8b-instruct:free (Très bon, gratuit)
    #   - google/gemma-2-9b-it:free (Gratuit)
    #   - mistralai/mistral-7b-instruct:free (Gratuit)
    # Modèles PAYANTS (meilleurs):
    #   - anthropic/claude-3.5-sonnet (~$3/M tokens)
    #   - openai/gpt-4-turbo (~$10/M tokens)
    #   - google/gemini-pro-1.5 (~$1.25/M tokens)
    model = os.getenv("OPENROUTER_MODEL", "meta-llama/llama-3.1-8b-instruct:free")

    response = client.chat.completions.create(
        model=model,
        messages=[
            {"role": "system", "content": system_prompt},
            {"role": "user", "content": user_prompt}
        ],
        max_tokens=max_tokens,
        extra_headers={
            "HTTP-Referer": "http://localhost:8000",  # Optionnel - pour le classement
            "X-Title": "StoryTeller App",  # Optionnel - s'affiche dans le dashboard OpenRouter
        }
    )

    return response.choices[0].message.content


async def call_ollama(system_prompt: str, user_prompt: str, model: str = "mistral") -> str:
    """Appelle Ollama local"""
    try:
        import httpx
    except ImportError:
        raise HTTPException(503, "Module 'httpx' non installé. Exécutez: pip install httpx")

    async with httpx.AsyncClient() as client:
        response = await client.post(
            f"{OLLAMA_URL}/api/generate",
            json={
                "model": model,
                "prompt": f"{system_prompt}\n\nUser: {user_prompt}\n\nAssistant:",
                "stream": False
            },
            timeout=120.0
        )

        if response.status_code != 200:
            raise HTTPException(503, f"Ollama error: {response.text}")

        return response.json()["response"]


async def generate_text(system_prompt: str, user_prompt: str, max_tokens: int = 2000) -> str:
    """Génère du texte avec le LLM configuré"""
    if LLM_PROVIDER == "anthropic":
        return await call_anthropic(system_prompt, user_prompt, max_tokens)
    elif LLM_PROVIDER == "openai":
        return await call_openai(system_prompt, user_prompt, max_tokens)
    elif LLM_PROVIDER == "openrouter":
        return await call_openrouter(system_prompt, user_prompt, max_tokens)
    elif LLM_PROVIDER == "ollama":
        return await call_ollama(system_prompt, user_prompt)
    else:
        raise HTTPException(400, f"LLM provider '{LLM_PROVIDER}' non supporté")


# ===== Routes =====

@router.post("/generate-chapter")
async def generate_chapter(request: ChapterGenerationRequest, db: Session = Depends(get_db)):
    """Génère un chapitre complet basé sur le contexte de l'histoire"""

    # Récupérer le contexte
    context = get_story_context(request.story_id, db)

    # Filtrer les personnages et lieux demandés
    selected_chars = []
    if request.include_characters:
        selected_chars = [c for c in context["characters"] if c.id in request.include_characters]

    selected_locs = []
    if request.include_locations:
        selected_locs = [l for l in context["locations"] if l.id in request.include_locations]

    # Construire le prompt système
    system_prompt = build_system_prompt(context)

    # Construire le prompt utilisateur
    length_guide = {
        "court": "500-800 mots",
        "moyen": "1000-1500 mots",
        "long": "2000-3000 mots"
    }

    user_prompt = f"""Écris un chapitre pour ce roman.

## Informations du chapitre
- Numéro: {request.chapter_number or 'À définir'}
- Titre: {request.chapter_title or 'À générer'}
- Longueur souhaitée: {length_guide.get(request.length, 'moyen')}
- Style: {request.style}
- Ton: {request.tone}
- Point de vue: {request.pov}

## Ce qui doit se passer
{request.summary}
"""

    if selected_chars:
        user_prompt += f"\n## Personnages à inclure\n"
        for char in selected_chars:
            user_prompt += f"- {char.name} {char.surname or ''}\n"

    if selected_locs:
        user_prompt += f"\n## Lieux à utiliser\n"
        for loc in selected_locs:
            user_prompt += f"- {loc.name}\n"

    user_prompt += "\n\nÉcris maintenant le chapitre complet en respectant toutes ces consignes."

    # Générer le texte
    try:
        generated_text = await generate_text(system_prompt, user_prompt, max_tokens=4000)

        return {
            "success": True,
            "text": generated_text,
            "chapter_number": request.chapter_number,
            "chapter_title": request.chapter_title,
            "word_count": len(generated_text.split())
        }
    except Exception as e:
        raise HTTPException(500, f"Erreur lors de la génération: {str(e)}")


@router.post("/continue-writing")
async def continue_writing(request: ContinueWritingRequest, db: Session = Depends(get_db)):
    """Continue l'écriture d'un manuscrit existant"""

    manuscript = db.query(Manuscript).filter(Manuscript.id == request.manuscript_id).first()
    if not manuscript:
        raise HTTPException(404, "Manuscrit introuvable")

    context = get_story_context(manuscript.story_id, db)
    system_prompt = build_system_prompt(context)

    user_prompt = f"""Voici le texte actuel du chapitre "{manuscript.title}":

{manuscript.text or ''}

---

Continue cette histoire dans cette direction: {request.direction}

Écris environ {request.length} mots supplémentaires qui s'intègrent naturellement à la suite du texte existant.
Ne répète pas ce qui a déjà été écrit. Commence directement la suite sans préambule.
"""

    try:
        continuation = await generate_text(system_prompt, user_prompt, max_tokens=2000)

        return {
            "success": True,
            "continuation": continuation,
            "word_count": len(continuation.split())
        }
    except Exception as e:
        raise HTTPException(500, f"Erreur lors de la continuation: {str(e)}")


@router.post("/rewrite")
async def rewrite_text(request: RewriteRequest, db: Session = Depends(get_db)):
    """Réécrit un texte selon des instructions"""

    system_prompt = "Tu es un assistant d'écriture expert. Tu réécris des textes selon les instructions données tout en préservant l'essence et le sens original."

    user_prompt = f"""Voici le texte à réécrire:

{request.text}

---

Instructions de réécriture: {request.instruction}

Réécris maintenant le texte en suivant ces instructions.
"""

    try:
        rewritten = await generate_text(system_prompt, user_prompt, max_tokens=2000)

        return {
            "success": True,
            "original": request.text,
            "rewritten": rewritten,
            "instruction": request.instruction
        }
    except Exception as e:
        raise HTTPException(500, f"Erreur lors de la réécriture: {str(e)}")


@router.post("/suggest-next-scene")
async def suggest_next_scene(request: SuggestNextSceneRequest, db: Session = Depends(get_db)):
    """Suggère des idées pour la prochaine scène"""

    context = get_story_context(request.story_id, db)
    system_prompt = build_system_prompt(context)

    user_prompt = f"""Situation actuelle dans l'histoire:
{request.current_situation}

Sur la base du synopsis, des personnages, et de la chronologie, suggère 5 idées différentes pour la prochaine scène.
Pour chaque idée, donne:
1. Un titre accrocheur
2. Une description en 2-3 phrases
3. Les personnages impliqués
4. L'impact potentiel sur l'intrigue

Formate ta réponse en JSON avec cette structure:
{{
  "suggestions": [
    {{
      "title": "...",
      "description": "...",
      "characters": ["...", "..."],
      "impact": "..."
    }}
  ]
}}
"""

    try:
        response = await generate_text(system_prompt, user_prompt, max_tokens=2000)

        # Essayer de parser le JSON
        try:
            # Nettoyer la réponse si elle contient des backticks
            cleaned = response.strip()
            if cleaned.startswith("```json"):
                cleaned = cleaned[7:]
            if cleaned.startswith("```"):
                cleaned = cleaned[3:]
            if cleaned.endswith("```"):
                cleaned = cleaned[:-3]
            cleaned = cleaned.strip()

            suggestions = json.loads(cleaned)
            return suggestions
        except json.JSONDecodeError:
            # Si le JSON est invalide, retourner le texte brut
            return {
                "success": True,
                "raw_response": response,
                "note": "Le LLM n'a pas retourné un JSON valide"
            }
    except Exception as e:
        raise HTTPException(500, f"Erreur lors de la suggestion: {str(e)}")


@router.get("/health")
def llm_health():
    """Vérifie la disponibilité du LLM"""
    status = {
        "provider": LLM_PROVIDER,
        "configured": False
    }

    if LLM_PROVIDER == "anthropic":
        status["configured"] = bool(ANTHROPIC_API_KEY)
    elif LLM_PROVIDER == "openai":
        status["configured"] = bool(OPENAI_API_KEY)
    elif LLM_PROVIDER == "openrouter":
        status["configured"] = bool(OPENROUTER_API_KEY)
        if status["configured"]:
            status["model"] = os.getenv("OPENROUTER_MODEL", "meta-llama/llama-3.1-8b-instruct:free")
    elif LLM_PROVIDER == "ollama":
        status["configured"] = True
        status["url"] = OLLAMA_URL

    return status