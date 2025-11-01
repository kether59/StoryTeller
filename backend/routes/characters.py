from flask import Blueprint, request, jsonify
from ..database import db
from ..models import Character

bp = Blueprint('characters', __name__, url_prefix='/api/characters')

@bp.route('', methods=['GET'])
def list_characters():
    story_id = request.args.get('story_id')
    query = Character.query
    if story_id:
        query = query.filter_by(story_id=int(story_id))
    items = query.all()

    return jsonify([c.to_dict() for c in items])

@bp.route('', methods=['POST'])
def create_or_update_character():
    data = request.get_json() or {}
    cid = data.get('id')
    if cid:
        ch = Character.query.get(cid)
        if not ch:
            return jsonify({'error':'Not found'}), 404
    else:
        ch = Character()
        db.session.add(ch)

    ch.name = data.get('name')
    try:
        ch.age = int(data.get('age')) if data.get('age') not in (None, '') else None
    except Exception:
        ch.age = None

        # 🧩 Association à un roman
    if not data.get('story_id'):
        return jsonify({'error': 'story_id is required'}), 400

    ch.story_id = data.get('story_id')
    ch.born = data.get('born')
    ch.description = data.get('description')
    ch.personality = data.get('personality')
    ch.history = data.get('history')

    db.session.commit()
    return jsonify(ch.to_dict())

@bp.route('', methods=['DELETE'])
def delete_character():
    data = request.get_json() or {}
    cid = data.get('id')
    if not cid:
        return jsonify({'error':'id required'}), 400
    ch = Character.query.get(cid)
    if not ch:
        return jsonify({'error':'not found'}), 404
    db.session.delete(ch)
    db.session.commit()
    return jsonify({'ok': True})