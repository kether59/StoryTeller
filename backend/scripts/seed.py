from datetime import datetime
from ..database import db
from ..models import Story, Character, Location, LoreEntry, TimelineEvent, Manuscript

def seed_database(app):
    """
    Crée un jeu de données de test complet pour l'application StoryTeller,
    en utilisant les nouveaux modèles enrichis et les relations Many-to-Many.

    Ce script est conçu pour être exécuté dans le contexte de l'application Flask.
    """
    with app.app_context():
        print("--- Nettoyage et initialisation des données de test ---")

        # 1. Nettoyage des données existantes (pour un re-lancement facile)
        # Note: Supprimer l'histoire supprime en cascade tous les éléments liés
        # si les relations ont été correctement configurées dans models.py.
        db.session.query(Manuscript).delete()
        db.session.query(TimelineEvent).delete()
        db.session.query(LoreEntry).delete()
        db.session.query(Location).delete()
        db.session.query(Character).delete()
        db.session.query(Story).delete()
        db.session.commit()

        # 2. Création de l'Histoire (Story)
        story1 = Story(
            title="Le Sceptre des Échos",
            synopsis="Dans un monde où la mémoire est monnaie d'échange, Elara, une marchande d'échos, découvre un sceptre ancien capable de révéler des vérités oubliées, la jetant dans une quête contre l'Ordre Immuable.",
            blurb="Une quête pour la vérité dans l'ombre du passé. Osez vous souvenir."
        )
        db.session.add(story1)
        db.session.commit()
        print(f"✅ Histoire créée: '{story1.title}' (ID: {story1.id})")

        # 3. Création des Personnages (Character) - AVEC TOUS LES CHAMPS ENRICHIS
        char_elara = Character(
            story_id=story1.id,
            name="Vancian",
            surname='Elara',
            role="Protagoniste",
            age=25,
            born="27 Nuit-de-Pluie (Année 1024)",
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
            born="14 Jour-Clair (Année 1014)",
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

        db.session.add_all([char_elara, char_kellan])
        db.session.commit()
        print(f"✅ Personnages créés: '{char_elara.name}', '{char_kellan.name}'")


        # 4. Création des Lieux (Location)
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

        db.session.add_all([loc_capitale, loc_echobazar])
        db.session.commit()
        print("✅ Lieux créés: 'Aethel', 'Bazar des Échos'")


        # 5. Création des Entrées de Lore (LoreEntry)
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

        db.session.add_all([lore_magie, lore_faction])
        db.session.commit()
        print("✅ Entrées de Lore créées: 'Système d'Écho', 'L'Ordre Immuable'")


        # 6. Création des Événements Chronologiques (TimelineEvent)
        event_sceptre = TimelineEvent(
            story_id=story1.id,
            title="Découverte du Sceptre",
            date="Jour 1 du Roman",
            sort_order=100,
            summary="Elara achète un 'écho' particulier au Bazar, qui se révèle être la clé du Sceptre des Échos dans une ruine antique."
        )

        event_confrontation = TimelineEvent(
            story_id=story1.id,
            title="Course-poursuite à Aethel",
            date="Jour 3 du Roman",
            sort_order=120,
            summary="Kellan (l'Archiviste) localise Elara. Une confrontation a lieu dans les rues brumeuses d'Aethel."
        )

        db.session.add_all([event_sceptre, event_confrontation])
        db.session.commit()

        # Liaison des relations Many-to-Many TimelineEvent <-> Character
        event_sceptre.characters.append(char_elara)
        event_sceptre.location = loc_echobazar

        event_confrontation.characters.append(char_elara)
        event_confrontation.characters.append(char_kellan)
        event_confrontation.location = loc_capitale

        db.session.commit()
        print("✅ Événements Chronologiques créés et liés.")


        # 7. Création du Manuscrit
        ms_chp1 = Manuscript(
            story_id=story1.id,
            title="Écho et Argent",
            chapter=1,
            text="Le marché sentait le vieux cuir et la tristesse. Elara s'accroupit, inspectant l'écho qu'on lui tendait. Elle sentit une chose étrange, une force qui dépassait une simple mémoire. Ce n'était pas un simple écho. C'était la clé de sa vie.",
            status="Premier jet"
        )
        db.session.add(ms_chp1)
        db.session.commit()
        print(f"✅ Manuscrit créé: '{ms_chp1.title}'")

        print("--- Initialisation des données de test terminée avec succès ---")