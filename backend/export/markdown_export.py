from ..models import Character, Location, TimelineEvent, Story, Manuscript, LoreEntry
import requests
from flask import current_app # Garder cet import pour export_manuscript_annotated

def export_to_markdown():
    parts = []

    story = Story.query.first()
    if story:
        parts.append(f"# {story.title}\n")
        parts.append("## Synopsis\n")
        parts.append(story.synopsis or '_Non renseigné_')
        parts.append('\n')
        parts.append("## Quatrième de couverture\n")
        parts.append(story.blurb or '_Non renseigné_')
        parts.append('\n\n')

    # ------------------
    # Personnages
    # ------------------
    parts.append('## Personnages\n')
    for c in Character.query.all():
        parts.append(f"### {c.name} ({c.role or 'Non défini'})\n")

        parts.append("#### Infos de base\n")
        parts.append(f"- Âge : {c.age or 'Non défini'}\n- Né(e) : {c.born or 'Non défini'}\n")
        parts.append(f"\n#### Description Physique\n")
        parts.append(c.physical_description or '_Non renseigné_')
        parts.append(f"\n#### Personnalité et Histoire\n")
        parts.append(f"**Personnalité :**\n{c.personality or '_Non renseigné_'}\n")
        parts.append(f"\n**Passé :**\n{c.history or '_Non renseigné_'}\n")

        parts.append(f"\n#### Moteur Narratif\n")
        parts.append(f"- Motivation : {c.motivation or '_Non renseigné_'}\n") 
        parts.append(f"- Objectif : {c.goal or '_Non renseigné_'}\n")        
        parts.append(f"- Faille : {c.flaw or '_Non renseigné_'}\n")          
        parts.append(f"- Arc Narratif : {c.character_arc or '_Non renseigné_'}\n") 

        parts.append(f"\n#### Compétences & Notes\n")
        parts.append(f"**Compétences :**\n{c.skills or '_Non renseigné_'}\n")  
        parts.append(f"\n**Notes :**\n{c.notes or '_Non renseigné_'}\n")     
        parts.append('\n')

    # ------------------
    # Lieux (Locations)
    # ------------------
    parts.append('\n## Lieux (Locations)\n')
    for w in Location.query.all():
        parts.append(f"### {w.name} ({w.type or 'Lieu général'})\n")
        parts.append(w.summary or '_Non renseigné_')
        parts.append('\n')

    # ------------------
    # Lore / World-Building
    # ------------------
    parts.append('\n## Lore / World-Building\n')
    for l in LoreEntry.query.all():
        parts.append(f"### {l.title} (Catégorie: {l.category or 'Divers'})\n")
        parts.append(l.content or '_Non renseigné_')
        parts.append('\n')

    # ------------------
    # Chronologie
    # ------------------
    parts.append('\n## Chronologie\n')
    for e in TimelineEvent.query.all():
        char_names = [c.name for c in e.characters]
        chars_str = f" [Personnages: {', '.join(char_names)}]" if char_names else ""


        parts.append(f"### {e.title} ({e.date or 'Date non spécifiée'}){chars_str}\n")

        parts.append(e.summary or '_Non renseigné_')
        parts.append('\n')
        
    return '\n'.join(parts)


def export_manuscript_annotated(mid, mode='fast'):
    from flask import current_app


    with current_app.test_request_context():
        from ..routes.manuscript import analyze_manuscript
        report_resp = analyze_manuscript(mid)

    try:
        report = report_resp.get_json()
    except Exception:
        report = {}

    m = Manuscript.query.get(mid)
    if not m:
        return ''

    text = m.text or ''

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