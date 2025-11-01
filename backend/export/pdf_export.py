from io import BytesIO
from reportlab.lib.pagesizes import A4
from reportlab.platypus import SimpleDocTemplate, Paragraph, Spacer
from reportlab.lib.styles import getSampleStyleSheet
from .markdown_export import export_to_markdown


def export_to_pdf_bytes():
    # Récupère le texte Markdown
    text = export_to_markdown()

    # Crée un buffer mémoire pour le PDF
    buffer = BytesIO()
    doc = SimpleDocTemplate(buffer, pagesize=A4)
    styles = getSampleStyleSheet()

    story = []


    for line in text.split('\n'):
        if line.strip() == '':
            story.append(Spacer(1, 6))
        else:
            story.append(Paragraph(line, styles['Normal']))
            story.append(Spacer(1, 4))

    # Génération du PDF
    doc.build(story)

    buffer.seek(0)
    return buffer.getvalue()