from fastapi import APIRouter, Depends, HTTPException, Query
from sqlalchemy.orm import Session
from typing import List

from ..database import get_db
from ..models import TimelineEvent as TimelineEventModel
from ..schemas import TimelineEvent, TimelineEventCreate, TimelineEventUpdate

router = APIRouter(prefix="/api/timeline", tags=["timeline"])


@router.get("", response_model=List[TimelineEvent])
def list_timeline_events(
        story_id: int = Query(..., description="ID de l'histoire"),
        db: Session = Depends(get_db)
):
    """Récupère la liste de tous les événements pour une histoire."""
    events = db.query(TimelineEventModel) \
        .filter(TimelineEventModel.story_id == story_id) \
        .order_by(TimelineEventModel.sort_order) \
        .all()

    # Convertir en dict pour inclure les personnages
    return [
        {
            **event.to_dict()
        }
        for event in events
    ]


@router.post("", response_model=TimelineEvent, status_code=201)
def create_timeline_event(event: TimelineEventCreate, db: Session = Depends(get_db)):
    """Crée un nouveau événement chronologique."""
    event_data = event.dict()
    character_ids = event_data.pop('characters', [])

    db_event = TimelineEventModel(**event_data)
    db.add(db_event)
    db.flush()  # Pour obtenir l'ID sans commit

    # Ajouter les personnages
    if character_ids:
        db_event.set_characters(character_ids, db)

    db.commit()
    db.refresh(db_event)

    return db_event.to_dict()


@router.get("/{event_id}", response_model=TimelineEvent)
def get_timeline_event(event_id: int, db: Session = Depends(get_db)):
    """Récupère un événement par son ID."""
    event = db.query(TimelineEventModel).filter(TimelineEventModel.id == event_id).first()
    if not event:
        raise HTTPException(status_code=404, detail="Timeline event not found")
    return event.to_dict()


@router.put("/{event_id}", response_model=TimelineEvent)
def update_timeline_event(
        event_id: int,
        event_update: TimelineEventUpdate,
        db: Session = Depends(get_db)
):
    """Met à jour un événement existant."""
    event = db.query(TimelineEventModel).filter(TimelineEventModel.id == event_id).first()
    if not event:
        raise HTTPException(status_code=404, detail="Timeline event not found")

    update_data = event_update.dict(exclude_unset=True)
    character_ids = update_data.pop('characters', None)

    # Mise à jour des champs basiques
    for key, value in update_data.items():
        setattr(event, key, value)

    # Mise à jour des personnages si fourni
    if character_ids is not None:
        event.set_characters(character_ids, db)

    db.commit()
    db.refresh(event)
    return event.to_dict()


@router.delete("/{event_id}")
def delete_timeline_event(event_id: int, db: Session = Depends(get_db)):
    """Supprime un événement."""
    event = db.query(TimelineEventModel).filter(TimelineEventModel.id == event_id).first()
    if not event:
        raise HTTPException(status_code=404, detail="Timeline event not found")

    db.delete(event)
    db.commit()
    return {"ok": True}