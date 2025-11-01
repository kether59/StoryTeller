from flask import Blueprint, request, jsonify
from ..database import db
from ..models import Story

bp = Blueprint('story', __name__, url_prefix='/api/story')

@bp.route('', methods=['GET'])
def get_story():
    s = Story.query.first()
    if not s:
        return jsonify([])
    return jsonify([s.to_dict()])

@bp.route('', methods=['POST'])
def create_or_update_story():
    data = request.get_json() or {}
    s = Story.query.first()
    if not s:
        s = Story()
        db.session.add(s)
    s.title = data.get('title')
    s.synopsis = data.get('synopsis')
    s.blurb = data.get('blurb')
    db.session.commit()
    return jsonify(s.to_dict())