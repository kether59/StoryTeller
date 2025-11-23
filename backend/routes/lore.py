from fastapi import APIRouter, Depends, HTTPException, Query
from sqlalchemy.orm import Session
from typing import List

from ..database import get_db
from ..models import LoreEntry as LoreEntryModel
from ..schemas import LoreEntry, LoreEntryCreate, LoreEntryUpdate

router = APIRouter(prefix="/api/lore", tags=["lore"])


@router.get("", response_model=List[LoreEntry])
def list_lore_entries(
        story_id: int = Query(..., description="ID de l'histoire"),
        db: Session = Depends(get_db)
):
    """Récupère la liste de toutes les entrées de Lore pour une histoire."""
    entries = db.query(LoreEntryModel) \
        .filter(LoreEntryModel.story_id == story_id) \
        .order_by(LoreEntryModel.title) \
        .all()
    return entries


@router.post("", response_model=LoreEntry, status_code=201)
def create_lore_entry(entry: LoreEntryCreate, db: Session = Depends(get_db)):
    """Crée une nouvelle entrée de Lore."""
    db_entry = LoreEntryModel(**entry.dict())
    db.add(db_entry)
    db.commit()
    db.refresh(db_entry)
    return db_entry


@router.get("/{lore_id}", response_model=LoreEntry)
def get_lore_entry(lore_id: int, db: Session = Depends(get_db)):
    """Récupère une entrée de Lore par son ID."""
    entry = db.query(LoreEntryModel).filter(LoreEntryModel.id == lore_id).first()
    if not entry:
        raise HTTPException(status_code=404, detail="Lore entry not found")
    return entry


@router.put("/{lore_id}", response_model=LoreEntry)
def update_lore_entry(
        lore_id: int,
        entry_update: LoreEntryUpdate,
        db: Session = Depends(get_db)
):
    """Met à jour une entrée de Lore existante."""
    entry = db.query(LoreEntryModel).filter(LoreEntryModel.id == lore_id).first()
    if not entry:
        raise HTTPException(status_code=404, detail="Lore entry not found")

    update_data = entry_update.dict(exclude_unset=True)
    for key, value in update_data.items():
        setattr(entry, key, value)

    db.commit()
    db.refresh(entry)
    return entry


@router.delete("/{lore_id}")
def delete_lore_entry(lore_id: int, db: Session = Depends(get_db)):
    """Supprime une entrée de Lore."""
    entry = db.query(LoreEntryModel).filter(LoreEntryModel.id == lore_id).first()
    if not entry:
        raise HTTPException(status_code=404, detail="Lore entry not found")

    db.delete(entry)
    db.commit()
    return {"ok": True}