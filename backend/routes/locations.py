from fastapi import APIRouter, Depends, HTTPException, Query
from sqlalchemy.orm import Session
from typing import List

from ..database import get_db
from ..models import Location as LocationModel
from ..schemas import Location, LocationCreate, LocationUpdate

router = APIRouter(prefix="/api/locations", tags=["locations"])


@router.get("", response_model=List[Location])
def list_locations(
        story_id: int = Query(..., description="ID de l'histoire"),
        db: Session = Depends(get_db)
):
    """Récupère la liste de tous les lieux pour une histoire."""
    locations = db.query(LocationModel) \
        .filter(LocationModel.story_id == story_id) \
        .order_by(LocationModel.name) \
        .all()
    return locations


@router.post("", response_model=Location, status_code=201)
def create_location(location: LocationCreate, db: Session = Depends(get_db)):
    """Crée un nouveau lieu."""
    db_location = LocationModel(**location.dict())
    db.add(db_location)
    db.commit()
    db.refresh(db_location)
    return db_location


@router.get("/{location_id}", response_model=Location)
def get_location(location_id: int, db: Session = Depends(get_db)):
    """Récupère un lieu par son ID."""
    location = db.query(LocationModel).filter(LocationModel.id == location_id).first()
    if not location:
        raise HTTPException(status_code=404, detail="Location not found")
    return location


@router.put("/{location_id}", response_model=Location)
def update_location(
        location_id: int,
        location_update: LocationUpdate,
        db: Session = Depends(get_db)
):
    """Met à jour un lieu existant."""
    location = db.query(LocationModel).filter(LocationModel.id == location_id).first()
    if not location:
        raise HTTPException(status_code=404, detail="Location not found")

    update_data = location_update.dict(exclude_unset=True)
    for key, value in update_data.items():
        setattr(location, key, value)

    db.commit()
    db.refresh(location)
    return location


@router.delete("/{location_id}")
def delete_location(location_id: int, db: Session = Depends(get_db)):
    """Supprime un lieu."""
    location = db.query(LocationModel).filter(LocationModel.id == location_id).first()
    if not location:
        raise HTTPException(status_code=404, detail="Location not found")

    db.delete(location)
    db.commit()
    return {"ok": True}