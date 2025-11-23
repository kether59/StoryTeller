"""
Script de seed pour initialiser la base de données.
Emplacement : backend/scripts/seed.py
Usage depuis le dossier backend : python scripts/seed.py
"""
import sys
import os
from pathlib import Path

# --- CORRECTION CRITIQUE DES CHEMINS ---
# 1. Dossier du script (.../backend/scripts)
script_dir = Path(__file__).resolve().parent

# 2. Dossier backend (.../backend)
backend_dir = script_dir.parent

# 3. Racine du projet (.../StoryTeller) -> C'est ICI qu'on doit être pour importer "backend"
project_root = backend_dir.parent

# 4. On ajoute la racine au chemin Python
if str(project_root) not in sys.path:
    sys.path.insert(0, str(project_root))

# --- MAINTENANT LES IMPORTS FONCTIONNENT ---
from backend.database import SessionLocal, engine, Base
from backend.models import Story, Character, Location, LoreEntry, TimelineEvent, Manuscript

def seed_database():
    """
    Crée un jeu de données de test complet pour l'application StoryTeller.
    """
    print(f"📂 Racine du projet détectée : {project_root}")
    print("--- Création des tables si nécessaire ---")

    # Création des tables (la localisation est gérée par config.py via database.py)
    Base.metadata.create_all(bind=engine)

    db = SessionLocal()

    try:
        print("--- Nettoyage et initialisation des données de test ---")

        # 1. Nettoyage
        db.query(Manuscript).delete()
        db.query(TimelineEvent).delete()
        db.query(LoreEntry).delete()
        db.query(Location).delete()
        db.query(Character).delete()
        db.query(Story).delete()
        db.commit()
        print("🧹 Anciennes données supprimées.")

        # 2. Création de l'Histoire (Story)
        story1 = Story(
            title="Le Sceptre des Échos",
            synopsis="Dans un monde où la mémoire est monnaie d'échange, Elara, une marchande d'échos, découvre un sceptre ancien capable de révéler des vérités oubliées, la jetant dans une quête contre l'Ordre Immuable.",
            blurb="Une quête pour la vérité dans l'ombre du passé. Osez vous souvenir."
        )
        db.add(story1)
        db.commit()
        db.refresh(story1)
        print(f"✅ Histoire créée: '{story1.title}' (ID: {story1.id})")

        # 3. Création des Personnages
        char_elara = Character(
            story_id=story1.id,
            name="Vancian",
            surname='Elara',
            role="Protagoniste",
            age=25,
            born="1024-06-27",
            physical_description="Petite, agile, cheveux châtains, yeux perçants, toujours vêtue de cuir souple.",
            personality="Sceptique, débrouillarde, loyaliste. A peur de l'autorité.",
            history="A grandi en vendant des échos (petits souvenirs collectés). Sa famille a été effacée par l'Ordre.",
            motivation="Démasquer l'Ordre Immuable et restaurer la mémoire collective.",
            goal="Retrouver le cœur du Sceptre avant la Nouvelle Lune.",
            flaw="Trop confiante envers les inconnus.",
            character_arc="Du marchand solitaire à la leader de la Rébellion.",
            skills="Aiguisée dans le commerce, couteau, compréhension des mécanismes anciens.",
            notes="Ses souvenirs d'enfance sont fragmentés, un indice caché."
        )

        char_kellan = Character(
            story_id=story1.id,
            name="Fogg",
            surname='Kellan',
            role="Antagoniste secondaire",
            age=35,
            born="1014-03-14",
            physical_description="Grand, froid, armure noire, masque facial. Ne montre jamais d'émotion.",
            personality="Impérieux, obsédé par l'Ordre et la pureté des 'Archives'.",
            history="Archiviste en chef, a vu son propre frère se faire effacer la mémoire.",
            motivation="Maintenir l'Ordre en effaçant tout ce qui est considéré comme dangereux.",
            goal="Capturer Elara et le Sceptre.",
            flaw="Incapable de reconnaître une vérité émotionnelle.",
            character_arc="De bras droit de l'Ordre à sa propre chute.",
            skills="Excellent combattant, expert en 'effacement de mémoire'.",
            notes="Est secrètement hanté par l'écho de son frère."
        )

        db.add_all([char_elara, char_kellan])
        db.commit()
        db.refresh(char_elara)
        db.refresh(char_kellan)
        print(f"✅ Personnages créés: '{char_elara.name}', '{char_kellan.name}'")

        # 4. Création des Lieux
        loc_capitale = Location(
            story_id=story1.id,
            name="Aethel, Cité de la Brume",
            type="Capitale",
            summary="La capitale du royaume, baignée d'une brume artificielle. Siège de l'Ordre Immuable et de la Grande Archive."
        )

        loc_echobazar = Location(
            story_id=story1.id,
            name="Le Bazar des Échos",
            type="Marché Noir",
            summary="Un marché souterrain illégal où les échos (souvenirs) sont vendus. Lieu d'opération d'Elara."
        )

        db.add_all([loc_capitale, loc_echobazar])
        db.commit()
        db.refresh(loc_capitale)
        db.refresh(loc_echobazar)
        print("✅ Lieux créés: 'Aethel', 'Bazar des Échos'")

        # 5. Création des Entrées de Lore
        lore_magie = LoreEntry(
            story_id=story1.id,
            title="Le Système d'Écho",
            category="Magie / Technologie",
            content="La magie repose sur l'exploitation des 'échos' (fragments de souvenirs cristallisés). Le Sceptre est le plus grand amplificateur d'échos connu."
        )

        lore_faction = LoreEntry(
            story_id=story1.id,
            title="L'Ordre Immuable",
            category="Faction",
            content="Le gouvernement dictatorial qui efface les souvenirs historiques et personnels pour créer une 'paix' basée sur l'ignorance."
        )

        db.add_all([lore_magie, lore_faction])
        db.commit()
        print("✅ Entrées de Lore créées: 'Système d'Écho', 'L'Ordre Immuable'")

        # 6. Création des Événements Chronologiques
        event_sceptre = TimelineEvent(
            story_id=story1.id,
            title="Découverte du Sceptre",
            date="2024-01-01",
            sort_order=100,
            summary="Elara achète un 'écho' particulier au Bazar, qui se révèle être la clé du Sceptre des Échos dans une ruine antique.",
            location_id=loc_echobazar.id
        )

        event_confrontation = TimelineEvent(
            story_id=story1.id,
            title="Course-poursuite à Aethel",
            date="2024-01-03",
            sort_order=120,
            summary="Kellan (l'Archiviste) localise Elara. Une confrontation a lieu dans les rues brumeuses d'Aethel.",
            location_id=loc_capitale.id
        )

        db.add_all([event_sceptre, event_confrontation])
        db.commit()
        db.refresh(event_sceptre)
        db.refresh(event_confrontation)

        # Liaison des personnages
        event_sceptre.characters.append(char_elara)
        event_confrontation.characters.append(char_elara)
        event_confrontation.characters.append(char_kellan)
        db.commit()

        print("✅ Événements Chronologiques créés et liés.")

        # 7. Création du Manuscrit
        ms_chp1 = Manuscript(
            story_id=story1.id,
            title="Écho et Argent",
            chapter=1,
            text="Le marché sentait le vieux cuir et la tristesse. Elara s'accroupit, inspectant l'écho qu'on lui tendait. Elle sentit une chose étrange, une force qui dépassait une simple mémoire. Ce n'était pas un simple écho. C'était la clé de sa vie.",
            status="Premier jet"
        )
        db.add(ms_chp1)
        db.commit()
        print(f"✅ Manuscrit créé: '{ms_chp1.title}'")

        print("\n" + "="*60)
        print("✅ Initialisation des données de test terminée avec succès!")
        print("="*60)
        print(f"📖 Histoire: {story1.title}")
        print(f"👥 Personnages: {len([char_elara, char_kellan])}")
        print(f"📍 Lieux: {len([loc_capitale, loc_echobazar])}")
        print(f"📚 Lore: {len([lore_magie, lore_faction])}")
        print(f"⏰ Événements: {len([event_sceptre, event_confrontation])}")
        print(f"📝 Manuscrits: 1")
        print("="*60 + "\n")

    except Exception as e:
        print(f"❌ Erreur lors du seed: {e}")
        db.rollback()
        raise
    finally:
        db.close()


if __name__ == "__main__":
    seed_database()
