from flask import Blueprint, request, jsonify
from ..models import Character, TimelineEvent
from datetime import datetime

bp = Blueprint('ai', __name__, url_prefix='/api/ai')

@bp.route('/suggest', methods=['POST'])
def suggest():
    data = request.get_json() or {}
    intent = data.get('intent','link_characters')
    if intent == 'link_characters':
        chars = Character.query.all()
        suggestions = []
        for i in range(len(chars)):
            for j in range(i+1, len(chars)):
                a = chars[i]; b = chars[j]
                name_a = (a.name or '').strip()
                name_b = (b.name or '').strip()
                last_a = name_a.split()[-1] if name_a else ''
                last_b = name_b.split()[-1] if name_b else ''
                if last_a and last_a == last_b:
                    suggestions.append({'type':'family','pair':[a.id,b.id],'reason':'Même nom de famille'})
                try:
                    if a.age is not None and b.age is not None and abs(a.age - b.age) <= 5:
                        suggestions.append({'type':'peer','pair':[a.id,b.id],'reason':f'Âges proches ({a.age} vs {b.age})'})
                except Exception:
                    pass
        return jsonify({'suggestions': suggestions})
    elif intent == 'timeline_conflicts':
        timeline = TimelineEvent.query.all()
        chars = Character.query.all()
        conflicts = []
        for ev in timeline:
            if not ev.date:
                continue
            try:
 
               ev_date = datetime.fromisoformat(ev.date)
            except Exception:
                continue
            for cid in ev.get_characters():
                ch = next((c for c in chars if c.id == cid), None)
                if ch and ch.born:
                    try:
                        born = datetime.fromisoformat(ch.born)
                        if born > ev_date:
                            conflicts.append({'event_id': ev.id, 'character_id': cid, 'reason': 'Personnage né après l\'événement'})
                    except Exception:
                        pass
        return jsonify({'conflicts': conflicts})
    else:
        return jsonify({'error':'unknown intent'}), 400