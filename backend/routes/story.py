from flask import Blueprint, request, jsonify
from ..database import db
from ..models import Story

bp = Blueprint('story', __name__, url_prefix='/api/stories')

@bp.route('', methods=['GET'])
def list_stories():
    """Récupère la liste de toutes les histoires (ou retourne la première pour l'instant)."""
    stories = Story.query.all()

    return jsonify([s.to_dict() for s in stories])

@bp.route('', methods=['POST'])
def create_story():
    """Crée une nouvelle histoire."""
    data = request.get_json() or {}

    if not data.get('title'):
        return jsonify({'error': 'Title is required'}), 400

    s = Story(
        title=data.get('title'),
        synopsis=data.get('synopsis'),
        blurb=data.get('blurb')
    )

    db.session.add(s)
    db.session.commit()

    return jsonify(s.to_dict()), 201

@bp.route('/<int:story_id>', methods=['GET'])
def get_single_story(story_id):
    """Récupère une seule histoire par son ID."""
    s = Story.query.get(story_id)
    if not s:
        return jsonify({'error': 'Story not found'}), 404
    return jsonify(s.to_dict())

@bp.route('/<int:story_id>', methods=['PUT'])
def update_story(story_id):
    """Met à jour une histoire existante par son ID."""
    s = Story.query.get(story_id)
    if not s:
        return jsonify({'error': 'Story not found'}), 404

    data = request.get_json() or {}

    s.title = data.get('title')
    s.synopsis = data.get('synopsis')
    s.blurb = data.get('blurb')

    db.session.commit()
    return jsonify(s.to_dict())

@bp.route('/<int:story_id>', methods=['DELETE'])
def delete_story(story_id):
    """Supprime une histoire et ses éléments liés (grâce au cascade)."""
    s = Story.query.get(story_id)
    if not s:
        return jsonify({'error': 'Story not found'}), 404

    db.session.delete(s)
    db.session.commit()
    return jsonify({'ok': True})