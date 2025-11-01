from flask import Flask, send_file, jsonify
from flask_cors import CORS
from .database import init_db, db
from .routes import characters, lore_entry, locations, timeline, story, ai, manuscript
from .export.markdown_export import export_to_markdown
from .export.pdf_export import export_to_pdf_bytes
from io import BytesIO


from .scripts.seed import seed_database


def create_app():
    app = Flask(__name__)
    CORS(app)
    init_db(app)

    app.register_blueprint(story.bp)
    app.register_blueprint(characters.bp)
    app.register_blueprint(locations.bp)
    app.register_blueprint(lore_entry.bp)
    app.register_blueprint(timeline.bp)
    app.register_blueprint(ai.bp)
    app.register_blueprint(manuscript.bp)

    with app.app_context():

        db.create_all()

        from .models import Story

        if Story.query.count() == 0:
            print("INFO: Aucune histoire trouvée. Lancement du seeding des données de test...")
            seed_database(app)
        else:
            print("INFO: Des données existent. Le script de seeding est ignoré.")


    @app.route('/')
    def home():
        return jsonify({'message':'storyteller backend running'})

    @app.route('/export/markdown')
    def export_md():
        md = export_to_markdown()
        return md, 200, {'Content-Type': 'text/markdown; charset=utf-8'}

    @app.route('/export/pdf')
    def export_pdf():
        data = export_to_pdf_bytes()
        return send_file(BytesIO(data), mimetype='application/pdf', as_attachment=True, download_name='storyteller_export.pdf')

    return app

if __name__ == '__main__':
    app = create_app()
    app.run(debug=True)
