from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from typing import List

from ..database import get_db
from ..models import Story as StoryModel
from ..schemas import Story, StoryCreate, StoryUpdate

router = APIRouter(prefix="/api/stories", tags=["stories"])


@router.get("", response_model=List[Story])
def list_stories(db: Session = Depends(get_db)):
    """Récupère la liste de toutes les histoires."""
    stories = db.query(StoryModel).all()
    return stories


@router.post("", response_model=Story, status_code=201)
def create_story(story: StoryCreate, db: Session = Depends(get_db)):
    """Crée une nouvelle histoire."""
    db_story = StoryModel(**story.dict())
    db.add(db_story)
    db.commit()
    db.refresh(db_story)
    return db_story


@router.get("/{story_id}", response_model=Story)
def get_story(story_id: int, db: Session = Depends(get_db)):
    """Récupère une seule histoire par son ID."""
    story = db.query(StoryModel).filter(StoryModel.id == story_id).first()
    if not story:
        raise HTTPException(status_code=404, detail="Story not found")
    return story


@router.put("/{story_id}", response_model=Story)
def update_story(story_id: int, story_update: StoryUpdate, db: Session = Depends(get_db)):
    """Met à jour une histoire existante."""
    story = db.query(StoryModel).filter(StoryModel.id == story_id).first()
    if not story:
        raise HTTPException(status_code=404, detail="Story not found")

    # Mise à jour des champs
    update_data = story_update.dict(exclude_unset=True)
    for key, value in update_data.items():
        setattr(story, key, value)

    db.commit()
    db.refresh(story)
    return story


@router.delete("/{story_id}")
def delete_story(story_id: int, db: Session = Depends(get_db)):
    """Supprime une histoire et ses éléments liés."""
    story = db.query(StoryModel).filter(StoryModel.id == story_id).first()
    if not story:
        raise HTTPException(status_code=404, detail="Story not found")

    db.delete(story)
    db.commit()
    return {"ok": True}