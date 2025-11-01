"""
Générateur de données de test pour StoryTeller
"""
from datetime import datetime, timedelta
import random


class TestDataGenerator:
    """Génère des données de test réalistes"""

    # Données de référence
    FIRST_NAMES_MALE = [
        "Jean", "Pierre", "Michel", "André", "Philippe",
        "Julien", "Thomas", "Alexandre", "Nicolas", "Maxime"
    ]

    FIRST_NAMES_FEMALE = [
        "Marie", "Sophie", "Claire", "Anne", "Julie",
        "Emma", "Léa", "Camille", "Sarah", "Laura"
    ]

    LAST_NAMES = [
        "Dupont", "Martin", "Bernard", "Dubois", "Thomas",
        "Robert", "Richard", "Petit", "Durand", "Leroy",
        "Moreau", "Simon", "Laurent", "Lefebvre", "Michel"
    ]

    OCCUPATIONS = [
        "médecin", "avocat", "professeur", "ingénieur",
        "artiste", "écrivain", "policier", "détective",
        "journaliste", "architecte", "chef cuisinier",
        "agriculteur", "commerçant", "artisan"
    ]

    SOCIAL_CLASSES = [
        "aristocratie", "bourgeoisie", "classe moyenne",
        "classe ouvrière", "paysannerie"
    ]

    ARCHETYPES = [
        "hero", "mentor", "ally", "threshold_guardian",
        "herald", "shapeshifter", "shadow", "trickster"
    ]

    SAMPLE_TEXTS = {
        'opening': """
            Le jour se levait à peine sur Paris lorsque Marie ouvrit les yeux. 
            Dans la rue, les premiers passants commençaient leur journée. 
            Elle regarda par la fenêtre, songeuse, en se demandant ce que cette 
            nouvelle journée lui réserverait.
        """,
        'dialogue': """
            — Tu es sûr de vouloir faire ça ? demanda Jean.
            — Certain, répondit Pierre avec détermination. C'est maintenant ou jamais.
            — Mais les risques...
            — Je sais. Mais je n'ai pas le choix.
        """,
        'action': """
            Il courut aussi vite que possible dans les ruelles sombres. 
            Derrière lui, les pas se rapprochaient inexorablement. 
            Son cœur battait à tout rompre. Il devait absolument atteindre 
            le pont avant eux.
        """
    }

    @classmethod
    def generate_character(
            cls,
            story_id: int = 1,
            gender: str = None,
            family_name: str = None,
            complete: bool = True
    ) -> dict:
        """Génère un personnage de test"""
        if gender is None:
            gender = random.choice(['male', 'female'])

        if gender == 'male':
            first_name = random.choice(cls.FIRST_NAMES_MALE)
        else:
            first_name = random.choice(cls.FIRST_NAMES_FEMALE)

        if family_name is None:
            family_name = random.choice(cls.LAST_NAMES)

        name = f"{first_name} {family_name}"
        age = random.randint(18, 70)
        born_year = datetime.now().year - age

        character = {
            'story_id': story_id,
            'name': name,
            'nickname': None,
            'age': age,
            'born': f"{born_year}-{random.randint(1,12):02d}-{random.randint(1,28):02d}",
            'gender': gender,
            'occupation': random.choice(cls.OCCUPATIONS),
            'social_class': random.choice(cls.SOCIAL_CLASSES),
        }

        if complete:
            character.update({
                'height': random.randint(155, 195),
                'weight': random.randint(50, 100),
                'physical_description': f"Personne de taille {'moyenne' if age < 50 else 'imposante'}, aux traits {'doux' if gender == 'female' else 'marqués'}.",
                'eye_color': random.choice(['bleu', 'vert', 'marron', 'noisette']),
                'hair_color': random.choice(['blond', 'brun', 'châtain', 'roux', 'blanc']),
                'build': random.choice(['slim', 'average', 'athletic', 'muscular']),

                'personality': f"Personnalité {random.choice(['introvertie', 'extravertie'])}, {random.choice(['calme', 'impulsif', 'réfléchi'])}.",
                'temperament': random.choice(['sanguine', 'choleric', 'melancholic', 'phlegmatic']),
                'strengths': f"Courageux, {random.choice(['intelligent', 'rusé', 'loyal'])}",
                'weaknesses': f"{random.choice(['Impulsif', 'Têtu', 'Méfiant'])}, parfois {random.choice(['arrogant', 'naïf'])}",
                'fears': f"Peur de {random.choice(['l\'échec', 'la solitude', 'la trahison', 'la mort'])}",
                'desires': f"Désire {random.choice(['la vengeance', 'la rédemption', 'la reconnaissance', 'l\'amour'])}",

                'backstory': f"Né(e) dans une famille {random.choice(['modeste', 'aisée'])}, a connu {random.choice(['des épreuves difficiles', 'une enfance heureuse'])}.",
                'role': random.choice(['protagonist', 'antagonist', 'supporting']),
                'archetype': random.choice(cls.ARCHETYPES),
                'importance': random.randint(5, 10),

                'core_belief': "La justice doit toujours triompher",
                'moral_alignment': random.choice([
                    'lawful_good', 'neutral_good', 'chaotic_good',
                    'lawful_neutral', 'true_neutral', 'chaotic_neutral'
                ]),
                'internal_conflict': "Lutte entre devoir et désir personnel",

                'skills': ['observation', 'persuasion', 'combat'],
                'languages': ['français', 'anglais'],
                'tags': ['protagoniste' if random.random() > 0.5 else 'secondaire']
            })

        return character

    @classmethod
    def generate_family(cls, story_id: int = 1, size: int = 3) -> list:
        """Génère une famille de personnages"""
        family_name = random.choice(cls.LAST_NAMES)
        family = []

        # Parents
        father = cls.generate_character(
            story_id=story_id,
            gender='male',
            family_name=family_name,
            complete=True
        )
        father['age'] = random.randint(40, 60)
        family.append(father)

        mother = cls.generate_character(
            story_id=story_id,
            gender='female',
            family_name=family_name,
            complete=True
        )
        mother['age'] = father['age'] + random.randint(-5, 5)
        family.append(mother)

        # Enfants
        for _ in range(size - 2):
            child = cls.generate_character(
                story_id=story_id,
                gender=random.choice(['male', 'female']),
                family_name=family_name,
                complete=True
            )
            child['age'] = random.randint(10, 25)
            family.append(child)

        return family

    @classmethod
    def generate_timeline_event(
            cls,
            story_id: int = 1,
            character_ids: list = None,
            date_offset_days: int = 0
    ) -> dict:
        """Génère un événement de chronologie"""
        event_date = datetime.now() - timedelta(days=date_offset_days)

        events_templates = [
            "Rencontre décisive",
            "Découverte importante",
            "Conflit majeur",
            "Révélation",
            "Point de non-retour"
        ]

        return {
            'story_id': story_id,
            'title': random.choice(events_templates),
            'date': event_date.strftime('%Y-%m-%d'),
            'summary': "Événement crucial dans le déroulement de l'histoire.",
            'characters': character_ids or []
        }

    @classmethod
    def generate_manuscript_text(cls, style: str = 'mixed', length: int = 500) -> str:
        """Génère un texte de manuscrit"""
        if style == 'dialogue':
            base = cls.SAMPLE_TEXTS['dialogue']
        elif style == 'action':
            base = cls.SAMPLE_TEXTS['action']
        else:
            base = '\n\n'.join(cls.SAMPLE_TEXTS.values())

        # Répéter pour atteindre la longueur souhaitée
        result = base
        while len(result.split()) < length:
            result += '\n\n' + random.choice(list(cls.SAMPLE_TEXTS.values()))

        return result

    @classmethod
    def generate_complete_story_data(cls) -> dict:
        """Génère un jeu de données complet pour tests"""
        story_id = 1

        # Personnages
        family = cls.generate_family(story_id, size=3)
        ally = cls.generate_character(story_id, complete=True)
        antagonist = cls.generate_character(story_id, complete=True)
        antagonist['role'] = 'antagonist'
        antagonist['archetype'] = 'shadow'

        characters = family + [ally, antagonist]

        # Événements
        events = [
            cls.generate_timeline_event(
                story_id,
                character_ids=[c['id'] for c in characters[:2]],
                date_offset_days=365 * i
            )
            for i in range(5)
        ]

        # Manuscrits
        manuscripts = [
            {
                'story_id': story_id,
                'title': f'Chapitre {i+1}',
                'chapter': i+1,
                'text': cls.generate_manuscript_text('mixed', length=300)
            }
            for i in range(3)
        ]

        return {
            'story': {
                'id': story_id,
                'title': 'Roman de Test',
                'synopsis': 'Une histoire captivante de test',
                'genre': 'Fiction',
                'target_word_count': 80000
            },
            'characters': characters,
            'events': events,
            'manuscripts': manuscripts
        }


# Exemples d'utilisation pour tests unitaires
if __name__ == '__main__':
    # Générer des personnages
    char1 = TestDataGenerator.generate_character(complete=True)
    print("Personnage généré:", char1['name'])

    # Générer une famille
    family = TestDataGenerator.generate_family(size=4)
    print(f"Famille générée: {len(family)} membres")
    print("Noms:", [c['name'] for c in family])

    # Données complètes
    full_data = TestDataGenerator.generate_complete_story_data()
    print(f"\nDonnées complètes générées:")
    print(f"- {len(full_data['characters'])} personnages")
    print(f"- {len(full_data['events'])} événements")
    print(f"- {len(full_data['manuscripts'])} chapitres")