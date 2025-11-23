"""
Script d'initialisation standalone de la base de données.
À placer à la RACINE du projet StoryTeller/ et exécuter avec: python init_db.py
"""
import sys
from pathlib import Path

# Ajouter le dossier backend au path
backend_path = Path(__file__).parent / "backend"
sys.path.insert(0, str(backend_path))

# Maintenant on peut importer
from datetime import datetime
from sqlalchemy.orm import Session
from database import SessionLocal, engine, Base
from models import Story, Character, Location, LoreEntry, TimelineEvent, Manuscript


def init_database():
    print("=" * 60)
    print("🚀 INITIALISATION DE LA BASE DE DONNÉES STORYTELLER")
    print("=" * 60)

    print("\n📦 Création des tables...")
    Base.metadata.create_all(bind=engine)
    print("✅ Tables créées avec succès\n")

    db: Session = SessionLocal()

    try:
        print("🧹 Nettoyage...")
        db.query(Manuscript).delete()
        db.query(TimelineEvent).delete()
        db.query(LoreEntry).delete()
        db.query(Location).delete()
        db.query(Character).delete()
        db.query(Story).delete()
        db.commit()
        print("✅ Données précédentes supprimées\n")

        print("📖 Création de l'histoire 'Le Sceptre des Échos'...")
        story = Story(
            title="Le Sceptre des Échos",
            synopsis="Dans un monde où la mémoire est monnaie d'échange, Elara découvre un sceptre ancien.",
            blurb="Une quête pour la vérité dans l'ombre du passé."
        )
        db.add(story)
        db.commit()
        db.refresh(story)
        print(f"✅ Histoire créée (ID: {story.id})\n")

        print("👥 Création des personnages...")
        elara = Character(
            story_id=story.id, name="Vancian", surname='Elara',
            role="Protagoniste", age=25, born="1024-06-27",
            physical_description="Petite, agile, cheveux châtains",
            personality="Sceptique, débrouillarde"
        )
        kellan = Character(
            story_id=story.id, name="Fogg", surname='Kellan',
            role="Antagoniste", age=35, born="1014-03-14",
            physical_description="Grand, froid, armure noire"
        )
        db.add_all([elara, kellan])
        db.commit()
        print("✅ 2 personnages créés\n")

        print("📍 Création des lieux...")
        aethel = Location(
            story_id=story.id, name="Aethel, Cité de la Brume",
            type="Capitale", summary="Capitale baignée de brume"
        )
        bazar = Location(
            story_id=story.id, name="Le Bazar des Échos",
            type="Marché Noir", summary="Marché souterrain illégal"
        )
        db.add_all([aethel, bazar])
        db.commit()
        print("✅ 2 lieux créés\n")

        print("📚 Création des entrées de lore...")
        lore1 = LoreEntry(
            story_id=story.id, title="Le Système d'Écho",
            category="Magie", content="La magie des échos cristallisés"
        )
        lore2 = LoreEntry(
            story_id=story.id, title="L'Ordre Immuable",
            category="Faction", content="Gouvernement dictatorial"
        )
        db.add_all([lore1, lore2])
        db.commit()
        print("✅ 2 entrées de lore créées\n")

        print("⏰ Création des événements...")
        db.refresh(bazar)
        db.refresh(aethel)
        db.refresh(elara)
        db.refresh(kellan)

        ev1 = TimelineEvent(
            story_id=story.id, title="Découverte du Sceptre",
            date="2024-01-01", sort_order=100,
            summary="Elara découvre le sceptre", location_id=bazar.id
        )
        ev2 = TimelineEvent(
            story_id=story.id, title="Course-poursuite",
            date="2024-01-03", sort_order=120,
            summary="Confrontation avec Kellan", location_id=aethel.id
        )
        db.add_all([ev1, ev2])
        db.commit()
        db.refresh(ev1)
        db.refresh(ev2)

        ev1.characters.append(elara)
        ev2.characters.extend([elara, kellan])
        db.commit()
        print("✅ 2 événements créés et liés\n")

        print("📝 Création du manuscrit...")
        ms = Manuscript(
            story_id=story.id, title="Écho et Argent", chapter=1,
            text="Le marché sentait le vieux cuir...", status="Premier jet"
        )
        db.add(ms)
        db.commit()
        print("✅ Manuscrit créé\n")

        print("=" * 60)
        print("🎉 SUCCÈS!")
        print("=" * 60)
        print("\n📊 Base de données initialisée avec:")
        print("   • 1 histoire")
        print("   • 2 personnages")
        print("   • 2 lieux")
        print("   • 2 entrées de lore")
        print("   • 2 événements")
        print("   • 1 manuscrit")
        print("\n🚀 Lancez le serveur:")
        print("   cd backend")
        print("   uvicorn main:app --reload")
        print("   Puis ouvrez: http://localhost:8000/docs")
        print("=" * 60 + "\n")

    except Exception as e:
        print(f"\n❌ ERREUR: {e}")
        import traceback
        traceback.print_exc()
        db.rollback()
    finally:
        db.close()


if __name__ == "__main__":
    init_database()