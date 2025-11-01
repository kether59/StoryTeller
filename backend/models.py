from datetime import datetime
from enum import Enum
from typing import Optional, List
from .database import db
import json


class GenderEnum(str, Enum):
    """Genre du personnage"""
    MALE = "male"
    FEMALE = "female"
    NON_BINARY = "non_binary"
    OTHER = "other"


class ArchetypeEnum(str, Enum):
    """Archétypes narratifs classiques"""
    HERO = "hero"
    MENTOR = "mentor"
    ALLY = "ally"
    THRESHOLD_GUARDIAN = "threshold_guardian"
    HERALD = "herald"
    SHAPESHIFTER = "shapeshifter"
    SHADOW = "shadow"
    TRICKSTER = "trickster"


class Story(db.Model):
    __tablename__ = 'stories'

    id = db.Column(db.Integer, primary_key=True)
    title = db.Column(db.String(200), nullable=False)
    synopsis = db.Column(db.Text)
    blurb = db.Column(db.Text)
    genre = db.Column(db.String(100))
    target_audience = db.Column(db.String(100))
    target_word_count = db.Column(db.Integer)
    current_word_count = db.Column(db.Integer, default=0)

    created_at = db.Column(db.DateTime, default=datetime.utcnow)
    updated_at = db.Column(db.DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)

    # Relations
    characters = db.relationship('Character', back_populates='story', cascade='all, delete-orphan')
    worlds = db.relationship('World', back_populates='story', cascade='all, delete-orphan')
    timeline_events = db.relationship('TimelineEvent', back_populates='story', cascade='all, delete-orphan')
    manuscripts = db.relationship('Manuscript', back_populates='story', cascade='all, delete-orphan')

    def to_dict(self):
        return {
            'id': self.id,
            'title': self.title,
            'synopsis': self.synopsis,
            'blurb': self.blurb,
            'genre': self.genre,
            'target_audience': self.target_audience,
            'target_word_count': self.target_word_count,
            'current_word_count': self.current_word_count,
            'created_at': self.created_at.isoformat() if self.created_at else None,
            'updated_at': self.updated_at.isoformat() if self.updated_at else None,
        }


class Character(db.Model):
    """Modèle enrichi pour personnage de roman"""
    __tablename__ = 'characters'

    # Identification
    id = db.Column(db.Integer, primary_key=True)
    story_id = db.Column(db.Integer, db.ForeignKey('stories.id'), nullable=False)

    # Informations de base
    name = db.Column(db.String(200), nullable=False)
    nickname = db.Column(db.String(200))
    age = db.Column(db.Integer)
    born = db.Column(db.String(30))  # Date de naissance
    died = db.Column(db.String(30))  # Date de mort (optionnel)
    gender = db.Column(db.String(20))  # male, female, non_binary, other

    # Apparence physique
    height = db.Column(db.Integer)  # en cm
    weight = db.Column(db.Integer)  # en kg
    physical_description = db.Column(db.Text)  # Description générale
    distinctive_features = db.Column(db.Text)  # Signes distinctifs (cicatrices, tatouages...)
    eye_color = db.Column(db.String(50))
    hair_color = db.Column(db.String(50))
    hair_style = db.Column(db.String(100))
    build = db.Column(db.String(50))  # corpulence: slim, athletic, muscular, heavy...

    # Caractère et psychologie
    personality = db.Column(db.Text)  # Traits de personnalité
    temperament = db.Column(db.String(50))  # sanguin, colérique, flegmatique, mélancolique
    mbti_type = db.Column(db.String(4))  # MBTI (optionnel)
    strengths = db.Column(db.Text)  # Forces
    weaknesses = db.Column(db.Text)  # Faiblesses
    fears = db.Column(db.Text)  # Peurs
    desires = db.Column(db.Text)  # Désirs profonds

    # Historique et développement
    history = db.Column(db.Text)  # Passé du personnage
    backstory = db.Column(db.Text)  # Backstory détaillé
    character_arc = db.Column(db.Text)  # Arc narratif du personnage

    # Rôle narratif
    role = db.Column(db.String(50))  # protagonist, antagonist, supporting, etc.
    archetype = db.Column(db.String(50))  # hero, mentor, ally, shadow, etc.
    importance = db.Column(db.Integer, default=5)  # 1-10

    # Relations et contexte
    occupation = db.Column(db.String(200))
    social_class = db.Column(db.String(100))
    education = db.Column(db.String(200))
    family_background = db.Column(db.Text)
    relationships_json = db.Column(db.Text, default='[]')  # Relations avec autres personnages

    # Compétences et talents
    skills_json = db.Column(db.Text, default='[]')  # Compétences spécifiques
    languages_json = db.Column(db.Text, default='[]')  # Langues parlées
    hobbies = db.Column(db.Text)

    # Psychologie et motivations
    core_belief = db.Column(db.Text)  # Croyance fondamentale
    moral_alignment = db.Column(db.String(50))  # lawful good, chaotic neutral, etc.
    internal_conflict = db.Column(db.Text)  # Conflit intérieur
    external_conflict = db.Column(db.Text)  # Conflit extérieur

    # Voix et expression
    speech_pattern = db.Column(db.Text)  # Façon de parler
    catchphrase = db.Column(db.String(200))  # Phrase fétiche
    accent = db.Column(db.String(100))

    # Métadonnées
    notes = db.Column(db.Text)  # Notes libres
    tags_json = db.Column(db.Text, default='[]')  # Tags pour catégorisation

    created_at = db.Column(db.DateTime, default=datetime.utcnow)
    updated_at = db.Column(db.DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)

    # Relations
    story = db.relationship('Story', back_populates='characters')

    def get_relationships(self) -> List[dict]:
        """Récupère les relations avec d'autres personnages"""
        try:
            return json.loads(self.relationships_json or '[]')
        except Exception:
            return []

    def set_relationships(self, relationships: List[dict]):
        """Définit les relations"""
        self.relationships_json = json.dumps(relationships, ensure_ascii=False)

    def get_skills(self) -> List[str]:
        try:
            return json.loads(self.skills_json or '[]')
        except Exception:
            return []

    def set_skills(self, skills: List[str]):
        self.skills_json = json.dumps(skills, ensure_ascii=False)

    def get_languages(self) -> List[str]:
        try:
            return json.loads(self.languages_json or '[]')
        except Exception:
            return []

    def set_languages(self, languages: List[str]):
        self.languages_json = json.dumps(languages, ensure_ascii=False)

    def get_tags(self) -> List[str]:
        try:
            return json.loads(self.tags_json or '[]')
        except Exception:
            return []

    def set_tags(self, tags: List[str]):
        self.tags_json = json.dumps(tags, ensure_ascii=False)

    def to_dict(self, include_relations: bool = False):
        """Convertit en dictionnaire"""
        data = {
            'id': self.id,
            'story_id': self.story_id,
            # Base
            'name': self.name,
            'nickname': self.nickname,
            'age': self.age,
            'born': self.born,
            'died': self.died,
            'gender': self.gender,
            # Physique
            'height': self.height,
            'weight': self.weight,
            'physical_description': self.physical_description,
            'distinctive_features': self.distinctive_features,
            'eye_color': self.eye_color,
            'hair_color': self.hair_color,
            'hair_style': self.hair_style,
            'build': self.build,
            # Psychologie
            'personality': self.personality,
            'temperament': self.temperament,
            'mbti_type': self.mbti_type,
            'strengths': self.strengths,
            'weaknesses': self.weaknesses,
            'fears': self.fears,
            'desires': self.desires,
            # Historique
            'history': self.history,
            'backstory': self.backstory,
            'character_arc': self.character_arc,
            # Rôle
            'role': self.role,
            'archetype': self.archetype,
            'importance': self.importance,
            # Contexte
            'occupation': self.occupation,
            'social_class': self.social_class,
            'education': self.education,
            'family_background': self.family_background,
            'relationships': self.get_relationships() if include_relations else [],
            'skills': self.get_skills(),
            'languages': self.get_languages(),
            'hobbies': self.hobbies,
            # Psychologie avancée
            'core_belief': self.core_belief,
            'moral_alignment': self.moral_alignment,
            'internal_conflict': self.internal_conflict,
            'external_conflict': self.external_conflict,
            # Voix
            'speech_pattern': self.speech_pattern,
            'catchphrase': self.catchphrase,
            'accent': self.accent,
            # Meta
            'notes': self.notes,
            'tags': self.get_tags(),
            'created_at': self.created_at.isoformat() if self.created_at else None,
            'updated_at': self.updated_at.isoformat() if self.updated_at else None,
        }
        return data


# Les autres modèles (World, TimelineEvent, Manuscript) restent similaires
# mais avec les mêmes améliorations (relations, validations, etc.)


class World(db.Model):
    __tablename__ = 'worlds'
    id = db.Column(db.Integer, primary_key=True)
    story_id = db.Column(db.Integer, db.ForeignKey('stories.id'), nullable=False)
    title = db.Column(db.String(200))
    type = db.Column(db.String(80))
    summary = db.Column(db.Text)

    def to_dict(self):
        return {'id': self.id,'story_id': self.story_id, 'title': self.title, 'type': self.type, 'summary': self.summary}

class TimelineEvent(db.Model):
    __tablename__ = 'timeline'
    id = db.Column(db.Integer, primary_key=True)
    story_id = db.Column(db.Integer, db.ForeignKey('stories.id'), nullable=False)
    title = db.Column(db.String(200))
    date = db.Column(db.String(50))  # ISO date string recommended
    summary = db.Column(db.Text)
    characters_json = db.Column(db.Text, default='[]')

    def get_characters(self):
        try:
            return json.loads(self.characters_json or '[]')
        except Exception:
            return []

    def set_characters(self, lst):
        self.characters_json = json.dumps(lst, ensure_ascii=False)

    def to_dict(self):
        return {'id': self.id,'story_id': self.story_id, 'title': self.title, 'date': self.date, 'summary': self.summary, 'characters': self.get_characters()}

class Manuscript(db.Model):
    __tablename__ = 'manuscripts'
    id = db.Column(db.Integer, primary_key=True)
    story_id = db.Column(db.Integer, db.ForeignKey('stories.id'), nullable=False)
    title = db.Column(db.String(250))
    chapter = db.Column(db.Integer, default=1)
    text = db.Column(db.Text)
    created_at = db.Column(db.DateTime, default=datetime.utcnow)
    updated_at = db.Column(db.DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)

    def to_dict(self):
        return {
            'id': self.id,
            'story_id': self.story_id,
            'title': self.title,
            'chapter': self.chapter,
            'text': self.text,
            'created_at': None if not self.created_at else self.created_at.isoformat(),
            'updated_at': None if not self.updated_at else self.updated_at.isoformat(),
        }

