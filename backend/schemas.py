from marshmallow import Schema, fields, validate, validates, ValidationError, post_load
from datetime import datetime


class CharacterSchema(Schema):
    """Schéma de validation pour Character"""

    # Champs requis
    id = fields.Int(dump_only=True)
    story_id = fields.Int(required=True)
    name = fields.Str(required=True, validate=validate.Length(min=1, max=200))

    # Informations de base
    nickname = fields.Str(validate=validate.Length(max=200))
    age = fields.Int(validate=validate.Range(min=0, max=200))
    born = fields.Str(validate=validate.Length(max=30))
    died = fields.Str(validate=validate.Length(max=30))
    gender = fields.Str(validate=validate.OneOf([
        'male', 'female', 'non_binary', 'other'
    ]))

    # Apparence physique
    height = fields.Int(validate=validate.Range(min=0, max=300))  # cm
    weight = fields.Int(validate=validate.Range(min=0, max=500))  # kg
    physical_description = fields.Str()
    distinctive_features = fields.Str()
    eye_color = fields.Str(validate=validate.Length(max=50))
    hair_color = fields.Str(validate=validate.Length(max=50))
    hair_style = fields.Str(validate=validate.Length(max=100))
    build = fields.Str(validate=validate.OneOf([
        'slim', 'average', 'athletic', 'muscular', 'heavy', 'petite'
    ]))

    # Psychologie
    personality = fields.Str()
    temperament = fields.Str(validate=validate.OneOf([
        'sanguine', 'choleric', 'melancholic', 'phlegmatic'
    ]))
    mbti_type = fields.Str(validate=validate.Regexp(r'^[IE][NS][TF][JP]$'))
    strengths = fields.Str()
    weaknesses = fields.Str()
    fears = fields.Str()
    desires = fields.Str()

    # Historique
    history = fields.Str()
    backstory = fields.Str()
    character_arc = fields.Str()

    # Rôle narratif
    role = fields.Str(validate=validate.OneOf([
        'protagonist', 'antagonist', 'supporting', 'minor'
    ]))
    archetype = fields.Str(validate=validate.OneOf([
        'hero', 'mentor', 'ally', 'threshold_guardian',
        'herald', 'shapeshifter', 'shadow', 'trickster'
    ]))
    importance = fields.Int(validate=validate.Range(min=1, max=10))

    # Contexte social
    occupation = fields.Str(validate=validate.Length(max=200))
    social_class = fields.Str(validate=validate.Length(max=100))
    education = fields.Str(validate=validate.Length(max=200))
    family_background = fields.Str()

    # Listes
    relationships = fields.List(fields.Dict())
    skills = fields.List(fields.Str())
    languages = fields.List(fields.Str())
    hobbies = fields.Str()

    # Psychologie avancée
    core_belief = fields.Str()
    moral_alignment = fields.Str(validate=validate.OneOf([
        'lawful_good', 'neutral_good', 'chaotic_good',
        'lawful_neutral', 'true_neutral', 'chaotic_neutral',
        'lawful_evil', 'neutral_evil', 'chaotic_evil'
    ]))
    internal_conflict = fields.Str()
    external_conflict = fields.Str()

    # Expression
    speech_pattern = fields.Str()
    catchphrase = fields.Str(validate=validate.Length(max=200))
    accent = fields.Str(validate=validate.Length(max=100))

    # Meta
    notes = fields.Str()
    tags = fields.List(fields.Str())

    created_at = fields.DateTime(dump_only=True)
    updated_at = fields.DateTime(dump_only=True)

    @validates('born')
    def validate_born(self, value):
        """Valide la date de naissance"""
        if value:
            try:
                datetime.fromisoformat(value)
            except ValueError:
                raise ValidationError("Format de date invalide (attendu: YYYY-MM-DD)")

    @validates('died')
    def validate_died(self, value):
        """Valide la date de mort"""
        if value:
            try:
                datetime.fromisoformat(value)
            except ValueError:
                raise ValidationError("Format de date invalide (attendu: YYYY-MM-DD)")


class StorySchema(Schema):
    """Schéma de validation pour Story"""
    id = fields.Int(dump_only=True)
    title = fields.Str(required=True, validate=validate.Length(min=1, max=200))
    synopsis = fields.Str()
    blurb = fields.Str()
    genre = fields.Str(validate=validate.Length(max=100))
    target_audience = fields.Str(validate=validate.Length(max=100))
    target_word_count = fields.Int(validate=validate.Range(min=0))
    current_word_count = fields.Int(validate=validate.Range(min=0))
    created_at = fields.DateTime(dump_only=True)
    updated_at = fields.DateTime(dump_only=True)


class WorldSchema(Schema):
    """Schéma de validation pour World"""
    id = fields.Int(dump_only=True)
    story_id = fields.Int(required=True)
    title = fields.Str(required=True, validate=validate.Length(min=1, max=200))
    type = fields.Str(validate=validate.Length(max=80))
    summary = fields.Str()


class TimelineEventSchema(Schema):
    """Schéma de validation pour TimelineEvent"""
    id = fields.Int(dump_only=True)
    story_id = fields.Int(required=True)
    title = fields.Str(required=True, validate=validate.Length(min=1, max=200))
    date = fields.Str(validate=validate.Length(max=50))
    summary = fields.Str()
    characters = fields.List(fields.Int())

    @validates('date')
    def validate_date(self, value):
        """Valide le format de date"""
        if value:
            try:
                datetime.fromisoformat(value)
            except ValueError:
                raise ValidationError("Format de date invalide (attendu: YYYY-MM-DD)")


class ManuscriptSchema(Schema):
    """Schéma de validation pour Manuscript"""
    id = fields.Int(dump_only=True)
    story_id = fields.Int(required=True)
    title = fields.Str(required=True, validate=validate.Length(min=1, max=250))
    chapter = fields.Int(validate=validate.Range(min=1))
    text = fields.Str()
    created_at = fields.DateTime(dump_only=True)
    updated_at = fields.DateTime(dump_only=True)


# Instances à utiliser dans les routes
character_schema = CharacterSchema()
characters_schema = CharacterSchema(many=True)
story_schema = StorySchema()
stories_schema = StorySchema(many=True)
world_schema = WorldSchema()
worlds_schema = WorldSchema(many=True)
timeline_event_schema = TimelineEventSchema()
timeline_events_schema = TimelineEventSchema(many=True)
manuscript_schema = ManuscriptSchema()
manuscripts_schema = ManuscriptSchema(many=True)
