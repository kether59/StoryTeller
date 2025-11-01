from flask import Blueprint, request, jsonify, current_app
from ..database import db
from ..models import Manuscript, Character, TimelineEvent
from datetime import datetime
import spacy

bp = Blueprint('manuscript', __name__, url_prefix='/api/manuscript')

# lazy load spaCy model to reduce startup cost
_nlp = None

def get_nlp():
    global _nlp
    if _nlp is None:
        try:
            _nlp = spacy.load('fr_core_news_md')
        except Exception as e:
            current_app.logger.error('spaCy model not found. Run: python -m spacy download fr_core_news_md')
            raise
    return _nlp

@bp.route('', methods=['GET'])
def list_manuscripts():
    story_id = request.args.get('story_id')
    query = Manuscript.query
    if story_id:
        query = query.filter_by(story_id=int(story_id))
    items = query.order_by(Manuscript.chapter).all()
    return jsonify([m.to_dict() for m in items])

@bp.route('', methods=['POST'])
def create_or_update_manuscript():
    data = request.get_json() or {}
    mid = data.get('id')
    if mid:
        m = Manuscript.query.get(mid)
        if not m:
            return jsonify({'error':'Not found'}), 404
    else:
        m = Manuscript()
        db.session.add(m)

        # 🧩 Association à un roman
    if not data.get('story_id'):
        return jsonify({'error': 'story_id is required'}), 400

    m.story_id = data.get('story_id')
    m.title = data.get('title')
    m.chapter = int(data.get('chapter') or 1)
    m.text = data.get('text')
    m.status = data.get('status')
    db.session.commit()
    return jsonify(m.to_dict())

@bp.route('', methods=['DELETE'])
def delete_manuscript():
    data = request.get_json() or {}
    mid = data.get('id')
    if not mid:
        return jsonify({'error':'id required'}), 400
    m = Manuscript.query.get(mid)
    if not m:
        return jsonify({'error':'not found'}), 404
    db.session.delete(m)
    db.session.commit()
    return jsonify({'ok': True})

@bp.route('/analyze/<int:mid>')
def analyze_manuscript(mid):
    mode = (request.args.get('mode') or 'fast').lower()
    m = Manuscript.query.get(mid)
    if not m:
        return jsonify({'error':'not found'}), 404
    nlp = get_nlp()
    doc = nlp(m.text or '')

    ents = []
    for ent in doc.ents:
        ents.append({'text': ent.text, 'label': ent.label_, 'start': ent.start_char, 'end': ent.end_char, 'sent': ent.sent.text})

    summary = []
    characters = Character.query.all()
    char_names = [(c.id, (c.name or '').strip()) for c in characters]
    mentions = []
    for cid, name in char_names:
        if name and name in (m.text or ''):
            mentions.append({'character_id': cid, 'name': name})
    if mentions:
        summary.append({'type':'mentions', 'count': len(mentions), 'items': mentions})

    timeline = TimelineEvent.query.all()
    conflicts = []
    for ev in timeline:
        if not ev.date:
            continue
        try:
            ev_date = datetime.fromisoformat(ev.date)
        except Exception:
            continue
        for cid, name in char_names:
            if name and name in (m.text or ''):
                ch = next((c for c in characters if c.id == cid), None)
                if ch and ch.born:
                    try:
                        born = datetime.fromisoformat(ch.born)
                        if born > ev_date:
                            conflicts.append({'event_id': ev.id, 'character_id': cid, 'reason': 'Character born after event', 'event_date': ev.date, 'born': ch.born})
                    except Exception:
                        pass
    if conflicts:
        summary.append({'type':'conflicts','items':conflicts})

    # build detailed report
    report = {
        'id': m.id,
        'title': m.title,
        'chapter': m.chapter,
        'mode': mode,
        'summary': summary,
        'Status': m.status,
        'entities': ents,
        'text_length': len(m.text or ''),
    }

    # in detailed mode add tokenized sentences + entity positions
    if mode == 'detailed':
        sents = []
        for i, sent in enumerate(doc.sents):
            sent_ents = [
                {'text': ent.text, 'label': ent.label_, 'start': ent.start_char - sent.start_char, 'end': ent.end_char - sent.start_char}
                for ent in sent.ents
            ]
            sents.append({'index': i, 'text': sent.text, 'entities': sent_ents})
        report['sentences'] = sents

    return jsonify(report)