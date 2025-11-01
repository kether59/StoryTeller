from flask import Blueprint, request, jsonify
from ..database import db
from ..models import World

bp = Blueprint('world', __name__, url_prefix='/api/world')

@bp.route('/<int:story_id>', methods=['GET'])
def list_world(story_id):
    items = World.query.filter_by(story_id=story_id).all()
    return jsonify([w.to_dict() for w in items])

@bp.route('', methods=['POST'])
def create_or_update_world():
    data = request.get_json() or {}
    wid = data.get('id')
    if wid:
        w = World.query.get(wid)
        if not w:
            return jsonify({'error':'Not found'}), 404
    else:
        w = World()
        db.session.add(w)

        # 🧩 Association à un roman
    if not data.get('story_id'):
        return jsonify({'error': 'story_id is required'}), 400

    w.story_id = data.get('story_id')
    w.title = data.get('title')
    w.type = data.get('type')
    w.summary = data.get('summary')
    db.session.commit()
    return jsonify(w.to_dict())

@bp.route('', methods=['DELETE'])
def delete_world():
    data = request.get_json() or {}
    wid = data.get('id')
    if not wid:
        return jsonify({'error':'id required'}), 400
    w = World.query.get(wid)
    if not w:
        return jsonify({'error':'not found'}), 404
    db.session.delete(w)
    db.session.commit()
    return jsonify({'ok': True})