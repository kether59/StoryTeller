from fastapi import APIRouter, Depends, HTTPException, Query
from sqlalchemy.orm import Session
from typing import List

from ..database import get_db
from ..models import Character as CharacterModel
from ..schemas import Character, CharacterCreate, CharacterUpdate

router = APIRouter(prefix="/api/characters", tags=["characters"])


@router.get("", response_model=List[Character])
def list_characters(
        story_id: int = Query(..., description="ID de l'histoire"),
        db: Session = Depends(get_db)
):
    """Récupère la liste de tous les personnages pour une histoire."""
    characters = db.query(CharacterModel) \
        .filter(CharacterModel.story_id == story_id) \
        .order_by(CharacterModel.name) \
        .all()
    return characters


@router.post("", response_model=Character, status_code=201)
def create_character(character: CharacterCreate, db: Session = Depends(get_db)):
    """Crée un nouveau personnage."""
    db_character = CharacterModel(**character.dict())
    db.add(db_character)
    db.commit()
    db.refresh(db_character)
    return db_character


@router.get("/{character_id}", response_model=Character)
def get_character(character_id: int, db: Session = Depends(get_db)):
    """Récupère un personnage par son ID."""
    character = db.query(CharacterModel).filter(CharacterModel.id == character_id).first()
    if not character:
        raise HTTPException(status_code=404, detail="Character not found")
    return character


@router.put("/{character_id}", response_model=Character)
def update_character(
        character_id: int,
        character_update: CharacterUpdate,
        db: Session = Depends(get_db)
):
    """Met à jour un personnage existant."""
    character = db.query(CharacterModel).filter(CharacterModel.id == character_id).first()
    if not character:
        raise HTTPException(status_code=404, detail="Character not found")

    update_data = character_update.dict(exclude_unset=True)
    for key, value in update_data.items():
        setattr(character, key, value)

    db.commit()
    db.refresh(character)
    return character


@router.delete("/{character_id}")
def delete_character(character_id: int, db: Session = Depends(get_db)):
    """Supprime un personnage."""
    character = db.query(CharacterModel).filter(CharacterModel.id == character_id).first()
    if not character:
        raise HTTPException(status_code=404, detail="Character not found")

    db.delete(character)
    db.commit()
    return {"ok": True}