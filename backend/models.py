from datetime import datetime
from .database import db
import json

class Story(db.Model):
    __tablename__ = 'stories'
    id = db.Column(db.Integer, primary_key=True)
    title = db.Column(db.String(200))
    synopsis = db.Column(db.Text)
    blurb = db.Column(db.Text)

    def to_dict(self):
        return {'id': self.id, 'title': self.title, 'synopsis': self.synopsis, 'blurb': self.blurb}

class Character(db.Model):
    __tablename__ = 'characters'
    id = db.Column(db.Integer, primary_key=True)
    story_id = db.Column(db.Integer, db.ForeignKey('stories.id'), nullable=False)
    name = db.Column(db.String(200))
    age = db.Column(db.Integer)
    born = db.Column(db.String(30))
    description = db.Column(db.Text)
    personality = db.Column(db.Text)
    history = db.Column(db.Text)
    created_at = db.Column(db.DateTime, default=datetime.utcnow)
    updated_at = db.Column(db.DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)

    def to_dict(self):
        return {
            'id': self.id,
            'story_id': self.story_id,
            'name': self.name,
            'age': self.age,
            'born': self.born,
            'description': self.description,
            'personality': self.personality,
            'history': self.history,
            'created_at': None if not self.created_at else self.created_at.isoformat(),
            'updated_at': None if not self.updated_at else self.updated_at.isoformat(),
        }

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
    
