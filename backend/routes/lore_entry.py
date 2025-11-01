from flask import Blueprint, request, jsonify
from ..database import db
from ..models import LoreEntry

bp = Blueprint('lore', __name__, url_prefix='/api/lore')

# --- Helper Function ---
def _populate_lore_entry_from_data(le: LoreEntry, data: dict):
    """Remplit un objet LoreEntry à partir des données de la requête."""

    if not data.get('story_id'):
        raise ValueError('story_id is required')

    le.story_id = data.get('story_id')
    le.title = data.get('title')
    le.category = data.get('category')
    le.content = data.get('content')

    return le

# --- Routes ---

@bp.route('', methods=['GET'])
def list_lore_entries():
    """Récupère la liste de toutes les entrées de Lore pour une histoire."""
    story_id = request.args.get('story_id')
    if not story_id:
        return jsonify({'error': 'story_id parameter is required'}), 400

    query = LoreEntry.query.filter_by(story_id=int(story_id)).order_by(LoreEntry.title)
    items = query.all()

    return jsonify([item.to_dict() for item in items])

@bp.route('', methods=['POST'])
def create_lore_entry():
    """Crée une nouvelle entrée de Lore."""
    data = request.get_json() or {}

    try:
        le = LoreEntry()
        le = _populate_lore_entry_from_data(le, data)
    except ValueError as e:
        return jsonify({'error': str(e)}), 400

    db.session.add(le)
    db.session.commit()

    return jsonify(le.to_dict()), 201

@bp.route('/<int:lore_id>', methods=['PUT'])
def update_lore_entry(lore_id):
    """Met à jour une entrée de Lore existante."""
    le = LoreEntry.query.get(lore_id)
    if not le:
        return jsonify({'error': 'Lore entry not found'}), 404

    data = request.get_json() or {}

    try:
        le = _populate_lore_entry_from_data(le, data)
    except ValueError as e:
        return jsonify({'error': str(e)}), 400

    db.session.commit()
    return jsonify(le.to_dict())

@bp.route('/<int:lore_id>', methods=['DELETE'])
def delete_lore_entry(lore_id):
    """Supprime une entrée de Lore."""
    le = LoreEntry.query.get(lore_id)
    if not le:
        return jsonify({'error': 'Lore entry not found'}), 404

    db.session.delete(le)
    db.session.commit()
    return jsonify({'ok': True})