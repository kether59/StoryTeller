from flask import Blueprint, request, jsonify
from ..database import db
from ..models import TimelineEvent

bp = Blueprint('timeline', __name__, url_prefix='/api/timeline')

# --- LISTE des événements par story_id ---
@bp.route('/<int:story_id>', methods=['GET'])
def list_timeline(story_id):
    """Retourne la chronologie pour un roman spécifique"""
    items = TimelineEvent.query.filter_by(story_id=story_id).order_by(TimelineEvent.date).all()
    return jsonify([e.to_dict() for e in items])


# --- CRÉATION ou MISE À JOUR d’un événement ---
@bp.route('', methods=['POST'])
def create_or_update_event():
    data = request.get_json() or {}
    eid = data.get('id')

    if eid:
        e = TimelineEvent.query.get(eid)
        if not e:
            return jsonify({'error': 'Not found'}), 404
    else:
        e = TimelineEvent()
        db.session.add(e)

    # 🧩 Association à un roman
    if not data.get('story_id'):
        return jsonify({'error': 'story_id is required'}), 400

    e.story_id = data.get('story_id')
    e.title = data.get('title')
    e.date = data.get('date')
    e.summary = data.get('summary')

    chars = data.get('characters') or []
    e.set_characters(chars)

    db.session.commit()
    return jsonify(e.to_dict())


# --- SUPPRESSION d’un événement ---
@bp.route('', methods=['DELETE'])
def delete_event():
    data = request.get_json() or {}
    eid = data.get('id')
    if not eid:
        return jsonify({'error': 'id required'}), 400

    e = TimelineEvent.query.get(eid)
    if not e:
        return jsonify({'error': 'not found'}), 404

    db.session.delete(e)
    db.session.commit()
    return jsonify({'ok': True})
