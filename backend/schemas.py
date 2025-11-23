from pydantic import BaseModel
from typing import Optional, List


# Story Schemas
class StoryBase(BaseModel):
    title: str
    synopsis: Optional[str] = None
    blurb: Optional[str] = None

class StoryCreate(StoryBase):
    pass

class StoryUpdate(StoryBase):
    title: Optional[str] = None

class Story(StoryBase):
    id: int

    class Config:
        from_attributes = True


# Character Schemas
class CharacterBase(BaseModel):
    name: Optional[str] = None
    surname: Optional[str] = None
    role: Optional[str] = None
    age: Optional[int] = None
    born: Optional[str] = None
    physical_description: Optional[str] = None
    personality: Optional[str] = None
    history: Optional[str] = None
    motivation: Optional[str] = None
    goal: Optional[str] = None
    flaw: Optional[str] = None
    character_arc: Optional[str] = None
    skills: Optional[str] = None
    notes: Optional[str] = None

class CharacterCreate(CharacterBase):
    story_id: int
    name: str  # Requis à la création

class CharacterUpdate(CharacterBase):
    story_id: Optional[int] = None

class Character(CharacterBase):
    id: int
    story_id: int

    class Config:
        from_attributes = True


# Location Schemas
class LocationBase(BaseModel):
    name: Optional[str] = None
    type: Optional[str] = None
    summary: Optional[str] = None

class LocationCreate(LocationBase):
    story_id: int

class LocationUpdate(LocationBase):
    story_id: Optional[int] = None

class Location(LocationBase):
    id: int
    story_id: int

    class Config:
        from_attributes = True


# LoreEntry Schemas
class LoreEntryBase(BaseModel):
    title: Optional[str] = None
    category: Optional[str] = None
    content: Optional[str] = None

class LoreEntryCreate(LoreEntryBase):
    story_id: int

class LoreEntryUpdate(LoreEntryBase):
    story_id: Optional[int] = None

class LoreEntry(LoreEntryBase):
    id: int
    story_id: int

    class Config:
        from_attributes = True


# TimelineEvent Schemas
class TimelineEventBase(BaseModel):
    title: Optional[str] = None
    date: Optional[str] = None
    sort_order: Optional[int] = 0
    summary: Optional[str] = None
    location_id: Optional[int] = None
    characters: Optional[List[int]] = []

class TimelineEventCreate(TimelineEventBase):
    story_id: int

class TimelineEventUpdate(TimelineEventBase):
    story_id: Optional[int] = None

class TimelineEvent(TimelineEventBase):
    id: int
    story_id: int

    class Config:
        from_attributes = True


# Manuscript Schemas
class ManuscriptBase(BaseModel):
    title: Optional[str] = None
    chapter: Optional[int] = 1
    text: Optional[str] = None
    status: Optional[str] = None

class ManuscriptCreate(ManuscriptBase):
    story_id: int

class ManuscriptUpdate(ManuscriptBase):
    story_id: Optional[int] = None

class Manuscript(ManuscriptBase):
    id: int
    story_id: int

    class Config:
        from_attributes = True