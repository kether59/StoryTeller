from datetime import datetime
from .database import db
import json

class Story(db.Model):
    __tablename__ = 'stories'
    id = db.Column(db.Integer, primary_key=True)
    title = db.Column(db.String(200))
    synopsis = db.Column(db.Text)
    blurb = db.Column(db.Text)

    characters = db.relationship('Character', back_populates='story', lazy='dynamic', cascade="all, delete-orphan")
    locations = db.relationship('Location', back_populates='story', lazy='dynamic', cascade="all, delete-orphan")
    timeline_events = db.relationship('TimelineEvent', back_populates='story', lazy='dynamic', cascade="all, delete-orphan")
    manuscripts = db.relationship('Manuscript', back_populates='story', lazy='dynamic', cascade="all, delete-orphan")
    lore_entries = db.relationship('LoreEntry', back_populates='story', lazy='dynamic', cascade="all, delete-orphan")

    def to_dict(self):
        return {'id': self.id, 'title': self.title, 'synopsis': self.synopsis, 'blurb': self.blurb}

class Character(db.Model):
    __tablename__ = 'characters'
    id = db.Column(db.Integer, primary_key=True)
    story_id = db.Column(db.Integer, db.ForeignKey('stories.id'), nullable=False)

    story = db.relationship('Story', back_populates='characters')

    # --- Infos de base ---
    name = db.Column(db.String(200), nullable=False)
    surname = db.Column(db.String(200), nullable=False)
    role = db.Column(db.String(50)) # Ex: 'Protagoniste', 'Antagoniste', 'Secondaire'
    age = db.Column(db.Integer)
    born = db.Column(db.String(50))

    # --- Descriptions (plus spécifiques) ---
    physical_description = db.Column(db.Text) # Apparence, taille, etc.
    personality = db.Column(db.Text) # Traits de caractère, tics de langage
    history = db.Column(db.Text) # Son passé

    # --- Moteur du personnage (TRÈS IMPORTANT) ---
    motivation = db.Column(db.Text) # Qu'est-ce qui le fait avancer ?
    goal = db.Column(db.Text) # Quel est son objectif concret ?
    flaw = db.Column(db.Text) # Son principal défaut, son point faible
    character_arc = db.Column(db.Text) # Comment va-t-il évoluer (résumé)

    # --- Notes ---
    skills = db.Column(db.Text) # Compétences, pouvoirs magiques...
    notes = db.Column(db.Text) # Notes libres de l'auteur

    created_at = db.Column(db.DateTime, default=datetime.utcnow)
    updated_at = db.Column(db.DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)

    events = db.relationship('TimelineEvent', secondary='event_character_association', back_populates='characters')

    # ... (fonction to_dict) ...
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
            'notes': self.notes,
            'created_at': self.created_at.isoformat() if self.created_at else None,
            'updated_at': self.updated_at.isoformat() if self.updated_at else None,
        }

class Location(db.Model):
    __tablename__ = 'locations'
    id = db.Column(db.Integer, primary_key=True)
    story_id = db.Column(db.Integer, db.ForeignKey('stories.id'), nullable=False)

    story = db.relationship('Story', back_populates='locations')

    name = db.Column(db.String(200)) # Ex: "Paris", "La Forêt Interdite", "Vaisseau 'Le Nomade'"
    type = db.Column(db.String(80)) # Ex: "Ville", "Région", "Planète", "Bâtiment"
    summary = db.Column(db.Text) # Description du lieu

    # Relation : quels événements se passent ici ?
    events = db.relationship('TimelineEvent', back_populates='location', lazy='dynamic')

    def to_dict(self):
        return {'id': self.id,'story_id': self.story_id, 'name': self.name, 'type': self.type, 'summary': self.summary}


class LoreEntry(db.Model):
    __tablename__ = 'lore_entries'
    id = db.Column(db.Integer, primary_key=True)
    story_id = db.Column(db.Integer, db.ForeignKey('stories.id'), nullable=False)

    story = db.relationship('Story', back_populates='lore_entries')

    title = db.Column(db.String(200)) # Ex: "Le Système de Magie", "Faction : Les Manteaux Rouges", "Guerre de l'An 1000"

    category = db.Column(db.String(80)) # Ex: 'Magie', 'Faction', 'Histoire', 'Technologie', 'Culture', 'Faune/Flore'

    content = db.Column(db.Text) # Description complète de l'entrée de lore

    def to_dict(self):
        return {'id': self.id,'story_id': self.story_id, 'title': self.title, 'category': self.category, 'content': self.content}


event_character_association = db.Table('event_character_association',
                                       db.Column('event_id', db.Integer, db.ForeignKey('timeline.id'), primary_key=True),
                                       db.Column('character_id', db.Integer, db.ForeignKey('characters.id'), primary_key=True)
                                       )


class TimelineEvent(db.Model):
    __tablename__ = 'timeline'
    id = db.Column(db.Integer, primary_key=True)
    story_id = db.Column(db.Integer, db.ForeignKey('stories.id'), nullable=False)

    story = db.relationship('Story', back_populates='timeline_events')

    title = db.Column(db.String(200))

    date = db.Column(db.String(50))
    sort_order = db.Column(db.Integer, default=0)

    summary = db.Column(db.Text)

    location_id = db.Column(db.Integer, db.ForeignKey('locations.id'), nullable=True)
    location = db.relationship('Location', back_populates='events')

    characters = db.relationship('Character', secondary=event_character_association, back_populates='events')

    def to_dict(self):
        return {
            'id': self.id,
            'story_id': self.story_id,
            'title': self.title,
            'date': self.date,
            'sort_order': self.sort_order,
            'summary': self.summary,
            'location_id': self.location_id,
            'characters': [char.id for char in self.characters]
        }

class Manuscript(db.Model):
    __tablename__ = 'manuscripts'
    id = db.Column(db.Integer, primary_key=True)
    story = db.relationship('Story', back_populates='manuscripts') # Ajout de la relation
    story_id = db.Column(db.Integer, db.ForeignKey('stories.id'), nullable=False)
    title = db.Column(db.String(250))
    chapter = db.Column(db.Integer, default=1)
    text = db.Column(db.Text)
    created_at = db.Column(db.DateTime, default=datetime.utcnow)
    updated_at = db.Column(db.DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)
    status = db.Column(db.String(50), default='Brouillon') # Ex: 'Brouillon', 'Premier jet', 'Corrigé', 'Final'

    def to_dict(self):
        return {
            'id': self.id,
            'story_id': self.story_id,
            'title': self.title,
            'chapter': self.chapter,
            'text': self.text,
            'status': self.status,
            'created_at': self.created_at.isoformat() if self.created_at else None,
            'updated_at': self.updated_at.isoformat() if self.updated_at else None,
        }
