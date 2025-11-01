from flask import Blueprint, request, jsonify
from ..database import db
from ..models import Location

bp = Blueprint('locations', __name__, url_prefix='/api/locations')

# --- Helper Function ---
def _populate_location_from_data(loc: Location, data: dict):
    """Remplit un objet Location à partir des données de la requête."""


    if not data.get('story_id'):
        raise ValueError('story_id is required')

    loc.story_id = data.get('story_id')
    loc.name = data.get('name')
    loc.type = data.get('type')
    loc.summary = data.get('summary')

    return loc

# --- Routes ---

@bp.route('', methods=['GET'])
def list_locations():
    """Récupère la liste de tous les lieux (Locations) pour une histoire."""
    story_id = request.args.get('story_id')
    if not story_id:
        return jsonify({'error': 'story_id parameter is required'}), 400

    query = Location.query.filter_by(story_id=int(story_id)).order_by(Location.name)
    items = query.all()

    return jsonify([item.to_dict() for item in items])

@bp.route('', methods=['POST'])
def create_location():
    """Crée un nouveau lieu."""
    data = request.get_json() or {}

    try:
        loc = Location()
        loc = _populate_location_from_data(loc, data)
    except ValueError as e:
        return jsonify({'error': str(e)}), 400

    db.session.add(loc)
    db.session.commit()

    return jsonify(loc.to_dict()), 201

@bp.route('/<int:location_id>', methods=['PUT'])
def update_location(location_id):
    """Met à jour un lieu existant."""
    loc = Location.query.get(location_id)
    if not loc:
        return jsonify({'error': 'Location not found'}), 404

    data = request.get_json() or {}

    try:
        # Note: On conserve l'appel, même si story_id ne devrait pas changer
        loc = _populate_location_from_data(loc, data)
    except ValueError as e:
        return jsonify({'error': str(e)}), 400

    db.session.commit()
    return jsonify(loc.to_dict())

@bp.route('/<int:location_id>', methods=['DELETE'])
def delete_location(location_id):
    """Supprime un lieu."""
    loc = Location.query.get(location_id)
    if not loc:
        return jsonify({'error': 'Location not found'}), 404

    db.session.delete(loc)
    db.session.commit()
    return jsonify({'ok': True})