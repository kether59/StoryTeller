from ..models import Character, World, TimelineEvent, Story, Manuscript
import requests

def export_to_markdown():
    parts = []

    story = Story.query.first()
    if story:
        parts.append(f"# {story.title}\n")
        parts.append("## Synopsis\n")
        parts.append(story.synopsis or '')
        parts.append('\n')
        parts.append("## Quatrième de couverture\n")
        parts.append(story.blurb or '')
        parts.append('\n\n')

    # Personnages
    parts.append('## Personnages\n')
    for c in Character.query.all():
        parts.append(f"### {c.name}\n")
        parts.append(f"- Âge : {c.age}\n- Né(e) : {c.born}\n")
        parts.append(c.description or '')
        parts.append('\n')

    # Monde
    parts.append('\n## Monde\n')
    for w in World.query.all():
        parts.append(f"### {w.title} ({w.type})\n")
        parts.append(w.summary or '')
        parts.append('\n')

    # Chronologie
    parts.append('\n## Chronologie\n')
    for e in TimelineEvent.query.all():
        parts.append(f"### {e.title} ({e.date})\n")
        parts.append(e.summary or '')
        parts.append('\n')

    return '\n'.join(parts)


def export_manuscript_annotated(mid, mode='fast'):
    from flask import current_app

    # Appel interne à la fonction d’analyse
    with current_app.test_request_context():
        from ..routes.manuscript import analyze_manuscript
        report_resp = analyze_manuscript(mid)

    # Extraction du JSON de la réponse Flask
    try:
        report = report_resp.get_json()
    except Exception:
        report = {}

    m = Manuscript.query.get(mid)
    if not m:
        return ''

    text = m.text or ''

    # Construction du texte annoté
    out = [f"# {m.title} (chapitre {m.chapter})\n"]
    out.append('## Texte original\n')
    out.append(text)
    out.append('\n\n## Analyse\n')
    out.append('### Résumé\n')
    out.append(str(report.get('summary', '')))
    out.append('\n\n### Entités\n')

    for ent in report.get('entities', []):
        out.append(f"- {ent.get('text')} ({ent.get('label')}) — phrase : {ent.get('sent')}")

    return '\n'.join(out)