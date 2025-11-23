from sqlalchemy import Column, Integer, String, Text, ForeignKey, Table
from sqlalchemy.orm import relationship

try:
    from .database import Base
except ImportError:
    from database import Base


# Table de liaison Many-to-Many pour Timeline <-> Character
timeline_character_association = Table(
    'timeline_character',
    Base.metadata,
    Column('timeline_event_id', Integer, ForeignKey('timeline_events.id', ondelete='CASCADE')),
    Column('character_id', Integer, ForeignKey('characters.id', ondelete='CASCADE'))
)


class Story(Base):
    __tablename__ = 'stories'

    id = Column(Integer, primary_key=True, index=True)
    title = Column(String(255), nullable=False)
    synopsis = Column(Text)
    blurb = Column(Text)

    # Relations
    characters = relationship('Character', back_populates='story', cascade='all, delete-orphan')
    locations = relationship('Location', back_populates='story', cascade='all, delete-orphan')
    lore_entries = relationship('LoreEntry', back_populates='story', cascade='all, delete-orphan')
    timeline_events = relationship('TimelineEvent', back_populates='story', cascade='all, delete-orphan')
    manuscripts = relationship('Manuscript', back_populates='story', cascade='all, delete-orphan')

    def to_dict(self):
        return {
            'id': self.id,
            'title': self.title,
            'synopsis': self.synopsis,
            'blurb': self.blurb
        }


class Character(Base):
    __tablename__ = 'characters'

    id = Column(Integer, primary_key=True, index=True)
    story_id = Column(Integer, ForeignKey('stories.id', ondelete='CASCADE'), nullable=False)
    name = Column(String(255))
    surname = Column(String(255))
    role = Column(String(255))
    age = Column(Integer)
    born = Column(String(50))
    physical_description = Column(Text)
    personality = Column(Text)
    history = Column(Text)
    motivation = Column(Text)
    goal = Column(Text)
    flaw = Column(Text)
    character_arc = Column(Text)
    skills = Column(Text)
    notes = Column(Text)

    # Relations
    story = relationship('Story', back_populates='characters')
    timeline_events = relationship('TimelineEvent', secondary=timeline_character_association, back_populates='characters')

    def to_dict(self):
        return {
            'id': self.id,
            'story_id': self.story_id,
            'name': self.name,
            'surname': self.surname,
            'role': self.role,
            'age': self.age,
            'born': self.born,
            'physical_description': self.physical_description,
            'personality': self.personality,
            'history': self.history,
            'motivation': self.motivation,
            'goal': self.goal,
            'flaw': self.flaw,
            'character_arc': self.character_arc,
            'skills': self.skills,
            'notes': self.notes
        }


class Location(Base):
    __tablename__ = 'locations'

    id = Column(Integer, primary_key=True, index=True)
    story_id = Column(Integer, ForeignKey('stories.id', ondelete='CASCADE'), nullable=False)
    name = Column(String(255))
    type = Column(String(100))
    summary = Column(Text)

    # Relations
    story = relationship('Story', back_populates='locations')
    timeline_events = relationship('TimelineEvent', back_populates='location')

    def to_dict(self):
        return {
            'id': self.id,
            'story_id': self.story_id,
            'name': self.name,
            'type': self.type,
            'summary': self.summary
        }


class LoreEntry(Base):
    __tablename__ = 'lore_entries'

    id = Column(Integer, primary_key=True, index=True)
    story_id = Column(Integer, ForeignKey('stories.id', ondelete='CASCADE'), nullable=False)
    title = Column(String(255))
    category = Column(String(100))
    content = Column(Text)

    # Relations
    story = relationship('Story', back_populates='lore_entries')

    def to_dict(self):
        return {
            'id': self.id,
            'story_id': self.story_id,
            'title': self.title,
            'category': self.category,
            'content': self.content
        }


class TimelineEvent(Base):
    __tablename__ = 'timeline_events'

    id = Column(Integer, primary_key=True, index=True)
    story_id = Column(Integer, ForeignKey('stories.id', ondelete='CASCADE'), nullable=False)
    title = Column(String(255))
    date = Column(String(50))
    sort_order = Column(Integer, default=0)
    summary = Column(Text)
    location_id = Column(Integer, ForeignKey('locations.id', ondelete='SET NULL'))

    # Relations
    story = relationship('Story', back_populates='timeline_events')
    location = relationship('Location', back_populates='timeline_events')
    characters = relationship('Character', secondary=timeline_character_association, back_populates='timeline_events')

    def get_characters(self):
        """Retourne la liste des IDs des personnages liés"""
        return [c.id for c in self.characters]

    def set_characters(self, character_ids, db_session):
        """Définit les personnages liés à partir d'une liste d'IDs"""
        self.characters = []
        for cid in character_ids:
            char = db_session.query(Character).filter(Character.id == cid).first()
            if char:
                self.characters.append(char)

    def to_dict(self):
        return {
            'id': self.id,
            'story_id': self.story_id,
            'title': self.title,
            'date': self.date,
            'sort_order': self.sort_order,
            'summary': self.summary,
            'location_id': self.location_id,
            'characters': self.get_characters()
        }


class Manuscript(Base):
    __tablename__ = 'manuscripts'

    id = Column(Integer, primary_key=True, index=True)
    story_id = Column(Integer, ForeignKey('stories.id', ondelete='CASCADE'), nullable=False)
    title = Column(String(255))
    chapter = Column(Integer, default=1)
    text = Column(Text)
    status = Column(String(50))

    # Relations
    story = relationship('Story', back_populates='manuscripts')

    def to_dict(self):
        return {
            'id': self.id,
            'story_id': self.story_id,
            'title': self.title,
            'chapter': self.chapter,
            'text': self.text,
            'status': self.status
        }