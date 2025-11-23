from fastapi import APIRouter, Depends, HTTPException, Query
from sqlalchemy.orm import Session
from typing import List, Optional
from datetime import datetime
import logging

from ..database import get_db
from ..models import Manuscript as ManuscriptModel, Character, TimelineEvent
from ..schemas import Manuscript, ManuscriptCreate, ManuscriptUpdate

router = APIRouter(prefix="/api/manuscript", tags=["manuscript"])
logger = logging.getLogger(__name__)

# Lazy load spaCy
_nlp = None

def get_nlp():
    global _nlp
    if _nlp is None:
        try:
            import spacy
            _nlp = spacy.load('fr_core_news_md')
        except Exception as e:
            logger.error('spaCy model not found. Run: python -m spacy download fr_core_news_md')
            raise HTTPException(
                status_code=500,
                detail="NLP model not available. Install with: python -m spacy download fr_core_news_md"
            )
    return _nlp


@router.get("", response_model=List[Manuscript])
def list_manuscripts(
        story_id: Optional[int] = Query(None, description="ID de l'histoire"),
        db: Session = Depends(get_db)
):
    """Récupère la liste de tous les manuscrits."""
    query = db.query(ManuscriptModel)
    if story_id:
        query = query.filter(ManuscriptModel.story_id == story_id)

    manuscripts = query.order_by(ManuscriptModel.chapter).all()
    return manuscripts


@router.post("", response_model=Manuscript, status_code=201)
def create_manuscript(manuscript: ManuscriptCreate, db: Session = Depends(get_db)):
    """Crée un nouveau manuscrit."""
    db_manuscript = ManuscriptModel(**manuscript.dict())
    db.add(db_manuscript)
    db.commit()
    db.refresh(db_manuscript)
    return db_manuscript


@router.get("/{manuscript_id}", response_model=Manuscript)
def get_manuscript(manuscript_id: int, db: Session = Depends(get_db)):
    """Récupère un manuscrit par son ID."""
    manuscript = db.query(ManuscriptModel).filter(ManuscriptModel.id == manuscript_id).first()
    if not manuscript:
        raise HTTPException(status_code=404, detail="Manuscript not found")
    return manuscript


@router.put("/{manuscript_id}", response_model=Manuscript)
def update_manuscript(
        manuscript_id: int,
        manuscript_update: ManuscriptUpdate,
        db: Session = Depends(get_db)
):
    """Met à jour un manuscrit existant."""
    manuscript = db.query(ManuscriptModel).filter(ManuscriptModel.id == manuscript_id).first()
    if not manuscript:
        raise HTTPException(status_code=404, detail="Manuscript not found")

    update_data = manuscript_update.dict(exclude_unset=True)
    for key, value in update_data.items():
        setattr(manuscript, key, value)

    db.commit()
    db.refresh(manuscript)
    return manuscript


@router.delete("/{manuscript_id}")
def delete_manuscript(manuscript_id: int, db: Session = Depends(get_db)):
    """Supprime un manuscrit."""
    manuscript = db.query(ManuscriptModel).filter(ManuscriptModel.id == manuscript_id).first()
    if not manuscript:
        raise HTTPException(status_code=404, detail="Manuscript not found")

    db.delete(manuscript)
    db.commit()
    return {"ok": True}


@router.get("/{manuscript_id}/analyze")
def analyze_manuscript(
        manuscript_id: int,
        mode: str = Query("fast", description="Mode d'analyse: 'fast' ou 'detailed'"),
        db: Session = Depends(get_db)
):
    """Analyse un manuscrit avec NLP (détection d'entités, mentions de personnages, etc.)."""
    manuscript = db.query(ManuscriptModel).filter(ManuscriptModel.id == manuscript_id).first()
    if not manuscript:
        raise HTTPException(status_code=404, detail="Manuscript not found")

    nlp = get_nlp()
    doc = nlp(manuscript.text or '')

    # Extraction des entités nommées
    entities = []
    for ent in doc.ents:
        entities.append({
            'text': ent.text,
            'label': ent.label_,
            'start': ent.start_char,
            'end': ent.end_char,
            'sentence': ent.sent.text
        })

    # Détection des mentions de personnages
    summary = []
    characters = db.query(Character).all()
    char_names = [(c.id, (c.name or '').strip()) for c in characters]
    mentions = []

    for cid, name in char_names:
        if name and name in (manuscript.text or ''):
            mentions.append({'character_id': cid, 'name': name})

    if mentions:
        summary.append({
            'type': 'mentions',
            'count': len(mentions),
            'items': mentions
        })

    # Détection des conflits chronologiques
    timeline = db.query(TimelineEvent).all()
    conflicts = []

    for ev in timeline:
        if not ev.date:
            continue
        try:
            ev_date = datetime.fromisoformat(ev.date)
        except Exception:
            continue

        for cid, name in char_names:
            if name and name in (manuscript.text or ''):
                ch = next((c for c in characters if c.id == cid), None)
                if ch and ch.born:
                    try:
                        born = datetime.fromisoformat(ch.born)
                        if born > ev_date:
                            conflicts.append({
                                'event_id': ev.id,
                                'character_id': cid,
                                'reason': 'Character born after event',
                                'event_date': ev.date,
                                'born': ch.born
                            })
                    except Exception:
                        pass

    if conflicts:
        summary.append({'type': 'conflicts', 'items': conflicts})

    # Rapport de base
    report = {
        'id': manuscript.id,
        'title': manuscript.title,
        'chapter': manuscript.chapter,
        'mode': mode,
        'summary': summary,
        'status': manuscript.status,
        'entities': entities,
        'text_length': len(manuscript.text or '')
    }

    # Mode détaillé : ajouter les phrases tokenisées
    if mode == 'detailed':
        sentences = []
        for i, sent in enumerate(doc.sents):
            sent_entities = [
                {
                    'text': ent.text,
                    'label': ent.label_,
                    'start': ent.start_char - sent.start_char,
                    'end': ent.end_char - sent.start_char
                }
                for ent in sent.ents
            ]
            sentences.append({
                'index': i,
                'text': sent.text,
                'entities': sent_entities
            })
        report['sentences'] = sentences

    return report