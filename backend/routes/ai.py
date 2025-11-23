import spacy
from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from pydantic import BaseModel
from typing import Literal, Optional, Dict, Any, List

from ..database import get_db
from ..models import Character, TimelineEvent, Location, LoreEntry, Manuscript

router = APIRouter(prefix="/api/ai", tags=["ai"])

try:
    nlp = spacy.load("fr_core_news_md")
except OSError:
    print("⚠️ Modèle spaCy 'fr_core_news_md' non trouvé. Certaines fonctions IA seront désactivées.")
    nlp = None

# -------------------------
#      REQUEST MODEL
# -------------------------
class SuggestRequest(BaseModel):
    intent: Literal[
        "link_characters",
        "timeline_conflicts",
        "script_consistency",
        "character_behavior",
        "lore_check"
    ]
    manuscript_id: Optional[int] = None

# -------------------------
#      CORE FUNCTIONS
# -------------------------

def find_mentions_in_doc(doc, names: list[str]) -> dict:
    """
    Optimisé : utilise l'objet doc déjà traité.
    Vérifie les limites de mots pour éviter que 'Tom' ne matche 'Tomate'.
    """
    found = {n: 0 for n in names}
    text_lower = doc.text.lower()

    # Approche simple par token pour exactitude
    # (Pour une approche pro, utiliser PhraseMatcher de spaCy)
    for token in doc:
        if token.text.lower() in [n.lower() for n in names]:
            # Retrouver la casse originale du nom pour le dictionnaire
            real_name = next((n for n in names if n.lower() == token.text.lower()), None)
            if real_name:
                found[real_name] += 1

    # Nettoyage des zéros
    return {k: v for k, v in found.items() if v > 0}

def analyze_behavior_syntax(doc, character: Character, issues: list):
    """
    Avancé : Vérifie si le personnage est le SUJET (nsubj) de l'action interdite.
    """
    personality_words = (character.personality or "").lower()
    char_first_name = character.name.split()[0].lower() if character.name else ""

    # Dictionnaire de règles simples
    rules = {
        "calme": ["crier", "hurler", "exploser", "frapper"],
        "pacifiste": ["tuer", "frapper", "attaquer", "combattre"],
        "timide": ["haranguer", "commander", "exiger"]
    }

    # On cherche les verbes dans le texte
    for token in doc:
        if token.pos_ == "VERB":
            lemma = token.lemma_.lower()

            # Vérifier si ce verbe est "interdit" pour un trait du perso
            for trait, forbidden_actions in rules.items():
                if trait in personality_words and lemma in forbidden_actions:

                    # C'est là que la magie opère : qui est le sujet ?
                    subjects = [child for child in token.children if child.dep_ == "nsubj"]

                    for subj in subjects:
                        # Si le sujet est notre personnage (ou un pronom référant, dur à dire sans coref, on reste simple)
                        if subj.text.lower() == char_first_name:
                            issues.append({
                                "character_id": character.id,
                                "char_name": character.name,
                                "action_found": lemma,
                                "conflicting_trait": trait,
                                "context": token.sent.text, # Donne la phrase complète pour contexte
                                "reason": f"Un personnage '{trait}' ne devrait pas '{lemma}'."
                            })

def check_lore_relevance(doc, lore_entries: List[LoreEntry]):
    """
    Modifié : Suggère des liens avec le Lore plutôt que de chercher des erreurs.
    """
    detected = []
    text_lower = doc.text.lower()

    for entry in lore_entries:
        # On cherche si le TITRE du lore apparaît dans le texte
        if entry.title.lower() in text_lower:
            detected.append({
                "lore_id": entry.id,
                "title": entry.title,
                "type": "mention",
                "info": "Ce concept du Lore est mentionné. Vérifiez sa cohérence."
            })
    return detected

def timeline_date_conflicts(events: list[TimelineEvent]):
    conflicts = []
    from datetime import datetime

    for e in events:
        if not e.date: continue
        try:
            event_date = datetime.fromisoformat(e.date)
        except ValueError: continue

        for ch in e.characters:
            # Check 1: Naissance
            if ch.born:
                try:
                    born = datetime.fromisoformat(ch.born)
                    if born > event_date:
                        conflicts.append({
                            "event_id": e.id,
                            "character_id": ch.id,
                            "reason": f"{ch.name} n'est pas encore né ({ch.born}) lors de l'événement ({e.date})"
                        })
                except ValueError: pass

            # Check 2 (Amélioration): Décès (si le champ existe un jour)
            # if ch.death and ...

    return conflicts

# -------------------------
#      MAIN ROUTE
# -------------------------
@router.post("/suggest")
def suggest(request: SuggestRequest, db: Session = Depends(get_db)) -> Dict[str, Any]:

    # 1. LINK CHARACTERS (Pas besoin de NLP)
    if request.intent == "link_characters":
        chars = db.query(Character).all()
        suggestions = []
        for i, a in enumerate(chars):
            for b in chars[i+1:]:
                # Nom de famille
                if a.surname and b.surname and a.surname.strip().lower() == b.surname.strip().lower():
                    suggestions.append({"type": "family", "pair": [a.id, b.id], "reason": f"Nom '{a.surname}' commun"})
                # Génération
                if a.age and b.age and abs(a.age - b.age) <= 5:
                    suggestions.append({"type": "peer", "pair": [a.id, b.id], "reason": "Même génération"})
        return {"suggestions": suggestions}

    # 2. TIMELINE (Pas besoin de NLP)
    if request.intent == "timeline_conflicts":
        events = db.query(TimelineEvent).all()
        return {"conflicts": timeline_date_conflicts(events)}

    # --- PRÉPARATION DU TEXTE POUR LES INTENTS SUIVANTS ---
    if not request.manuscript_id:
        raise HTTPException(400, "manuscript_id requis pour l'analyse textuelle")

    manuscript = db.query(Manuscript).filter_by(id=request.manuscript_id).first()
    if not manuscript:
        raise HTTPException(404, "Manuscrit introuvable")

    if not nlp:
        raise HTTPException(503, "Service NLP indisponible (Modèle non chargé)")

    # OPTIMISATION : On traite le texte UNE SEULE FOIS ici
    doc = nlp(manuscript.text or "")

    # 3. SCRIPT CONSISTENCY
    if request.intent == "script_consistency":
        characters = db.query(Character).all()
        lore = db.query(LoreEntry).all()
        char_names = [c.name for c in characters if c.name]

        return {
            "mentions": find_mentions_in_doc(doc, char_names),
            "lore_mentions": check_lore_relevance(doc, lore)
        }

    # 4. BEHAVIOR
    if request.intent == "character_behavior":
        characters = db.query(Character).all()
        issues = []
        for ch in characters:
            analyze_behavior_syntax(doc, ch, issues) # Utilise la syntaxe
        return {"behavior_issues": issues}

    # 5. LORE CHECK
    if request.intent == "lore_check":
        lore = db.query(LoreEntry).all()
        return {"lore_analysis": check_lore_relevance(doc, lore)}

    raise HTTPException(400, "Intent inconnu")