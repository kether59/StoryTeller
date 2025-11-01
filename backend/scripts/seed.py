from backend.app import create_app
from backend.database import db
from backend.models import Character, World, TimelineEvent, Story

app = create_app()

with app.app_context():
    db.create_all()
    if Character.query.count()==0:
        c1 = Character(name='Jean Dupont', age=45, born='1980-06-12', description='Grand, cheveux bruns', personality='Froid', history='...')
        c2 = Character(name='Marie Dupont', age=42, born='1983-02-28', description='Petite, yeux verts', personality='Chaleureuse', history='...')
        db.session.add_all([c1,c2])
    if World.query.count()==0:
        w = World(title='Ville d\'Aurore', type='ville', summary='Capitale du pays fictif')
        db.session.add(w)
    if TimelineEvent.query.count()==0:
        e = TimelineEvent(title='Attentat', date='2005-05-01', summary='Un événement marquant')
        e.set_characters([1])
        db.session.add(e)
    if Story.query.count()==0:
        s = Story(title='Titre exemple', synopsis='...', blurb='...')
        db.session.add(s)
    db.session.commit()
    print('Seed done')