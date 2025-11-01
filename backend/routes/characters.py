from flask import Blueprint, request, jsonify
from ..database import db
from ..models import Character

bp = Blueprint('characters', __name__, url_prefix='/api/characters')

# --- Helper Function (pour éviter la duplication de code) ---

def _populate_character_from_data(ch: Character, data: dict):
    """Remplit un objet Character à partir d'un dictionnaire de données."""

    try:
        age_raw = data.get('age')
        ch.age = int(age_raw) if age_raw not in (None, '', 'null') else None
    except (ValueError, TypeError):
        ch.age = None

    # --- Champs de base ---
    ch.name = data.get('name')
    ch.surname = data.get('surname')
    ch.story_id = data.get('story_id')
    ch.born = data.get('born')


    ch.role = data.get('role')
    ch.physical_description = data.get('physical_description')
    ch.personality = data.get('personality')
    ch.history = data.get('history')
    ch.motivation = data.get('motivation')
    ch.goal = data.get('goal')
    ch.flaw = data.get('flaw')
    ch.character_arc = data.get('character_arc')
    ch.skills = data.get('skills')
    ch.notes = data.get('notes')

    return ch

# --- ROUTES API (RESTful) ---

@bp.route('', methods=['GET'])
def list_characters():
    """Récupère la liste de tous les personnages pour une histoire."""
    story_id = request.args.get('story_id')
    if not story_id:
        return jsonify({'error': 'story_id parameter is required'}), 400

    query = Character.query.filter_by(story_id=int(story_id)).order_by(Character.name)
    items = query.all()

    return jsonify([c.to_dict() for c in items])

@bp.route('', methods=['POST'])
def create_character():
    """Crée un nouveau personnage."""
    data = request.get_json() or {}

    # --- Validation ---
    if not data.get('story_id'):
        return jsonify({'error': 'story_id is required'}), 400
    if not data.get('name'):
        return jsonify({'error': 'name is required'}), 400

    ch = Character()
    ch = _populate_character_from_data(ch, data)

    db.session.add(ch)
    db.session.commit()

    # 201 = "Created" (Bonne pratique)
    return jsonify(ch.to_dict()), 201

@bp.route('/<int:character_id>', methods=['PUT'])
def update_character(character_id):
    """Met à jour un personnage existant."""
    ch = Character.query.get(character_id)
    if not ch:
        return jsonify({'error': 'Character not found'}), 404

    data = request.get_json() or {}
    ch = _populate_character_from_data(ch, data)

    db.session.commit()
    return jsonify(ch.to_dict())

@bp.route('/<int:character_id>', methods=['DELETE'])
def delete_character(character_id):
    """Supprime un personnage."""
    ch = Character.query.get(character_id)
    if not ch:
        return jsonify({'error': 'Character not found'}), 404

    db.session.delete(ch)
    db.session.commit()
    return jsonify({'ok': True})