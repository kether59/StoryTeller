"""
Tests unitaires pour le service IA
"""
import unittest
from datetime import datetime, timedelta
from backend.services.ai_service import AIService
from backend.tests.test_data import TestDataGenerator


class TestAIService(unittest.TestCase):
    """Tests pour le service d'analyse IA"""

    @classmethod
    def setUpClass(cls):
        """Initialisation une fois pour tous les tests"""
        cls.ai = AIService()
        cls.test_data = TestDataGenerator.generate_complete_story_data()

    def test_analyze_character_relationships_family(self):
        """Test détection relations familiales"""
        # Générer une famille
        family = TestDataGenerator.generate_family(size=3)

        # Ajouter des IDs
        for i, char in enumerate(family):
            char['id'] = i + 1

        suggestions = self.ai.analyze_character_relationships(family)

        # Doit détecter des liens familiaux
        family_suggestions = [s for s in suggestions if s['type'] == 'family']
        self.assertGreater(len(family_suggestions), 0, "Devrait détecter des liens familiaux")

        # Vérifier la confiance
        for sugg in family_suggestions:
            self.assertGreaterEqual(sugg['confidence'], 0.7)

    def test_analyze_character_relationships_peers(self):
        """Test détection relations entre pairs (âges proches)"""
        # Créer deux personnages d'âges proches
        char1 = TestDataGenerator.generate_character(complete=True)
        char1['id'] = 1
        char1['age'] = 25

        char2 = TestDataGenerator.generate_character(complete=True)
        char2['id'] = 2
        char2['age'] = 27

        suggestions = self.ai.analyze_character_relationships([char1, char2])

        # Doit détecter une relation de pair
        peer_suggestions = [s for s in suggestions if s['type'] == 'peer']
        self.assertGreater(len(peer_suggestions), 0, "Devrait détecter des pairs")

    def test_analyze_character_relationships_professional(self):
        """Test détection relations professionnelles"""
        char1 = TestDataGenerator.generate_character(complete=True)
        char1['id'] = 1
        char1['occupation'] = 'médecin urgentiste'

        char2 = TestDataGenerator.generate_character(complete=True)
        char2['id'] = 2
        char2['occupation'] = 'médecin généraliste'

        suggestions = self.ai.analyze_character_relationships([char1, char2])

        # Doit détecter une relation professionnelle
        prof_suggestions = [s for s in suggestions if s['type'] == 'professional']
        self.assertGreater(len(prof_suggestions), 0, "Devrait détecter des relations professionnelles")

    def test_analyze_character_arc_complete(self):
        """Test analyse arc narratif complet"""
        char = TestDataGenerator.generate_character(complete=True)

        analysis = self.ai.analyze_character_arc_coherence(char)

        self.assertIn('score', analysis)
        self.assertIn('issues', analysis)
        self.assertIn('complete', analysis)

        # Un personnage complet devrait avoir un bon score
        self.assertGreater(analysis['score'], 70)

    def test_analyze_character_arc_incomplete(self):
        """Test analyse arc narratif incomplet"""
        char = TestDataGenerator.generate_character(complete=False)

        analysis = self.ai.analyze_character_arc_coherence(char)

        # Un personnage incomplet devrait avoir des issues
        self.assertGreater(len(analysis['issues']), 0)
        self.assertLess(analysis['score'], 100)

    def test_suggest_character_development(self):
        """Test suggestions de développement"""
        char = TestDataGenerator.generate_character(complete=True)
        char['archetype'] = 'hero'

        suggestions = self.ai.suggest_character_development(char)

        self.assertIsInstance(suggestions, list)
        # Devrait suggérer quelque chose pour un héros
        self.assertGreater(len(suggestions), 0)

    def test_check_timeline_consistency_born_after_event(self):
        """Test détection personnage né après événement"""
        # Créer un personnage
        char = TestDataGenerator.generate_character(complete=True)
        char['id'] = 1
        char['born'] = '2000-01-01'

        # Créer un événement avant sa naissance
        event = {
            'id': 1,
            'title': 'Événement test',
            'date': '1995-06-15',
            'characters': [1]
        }

        conflicts = self.ai.check_timeline_consistency([event], [char])

        # Doit détecter l'incohérence
        born_after = [c for c in conflicts if c['type'] == 'born_after_event']
        self.assertGreater(len(born_after), 0, "Devrait détecter personnage né après événement")

    def test_check_timeline_consistency_present_after_death(self):
        """Test détection personnage présent après sa mort"""
        char = TestDataGenerator.generate_character(complete=True)
        char['id'] = 1
        char['born'] = '1950-01-01'
        char['died'] = '2010-12-31'

        # Événement après sa mort
        event = {
            'id': 1,
            'title': 'Événement posthume',
            'date': '2015-06-15',
            'characters': [1]
        }

        conflicts = self.ai.check_timeline_consistency([event], [char])

        # Doit détecter l'incohérence
        after_death = [c for c in conflicts if c['type'] == 'present_after_death']
        self.assertGreater(len(after_death), 0, "Devrait détecter présence après la mort")

    def test_check_timeline_consistency_age_inconsistency(self):
        """Test détection incohérence d'âge"""
        char = TestDataGenerator.generate_character(complete=True)
        char['id'] = 1
        char['born'] = '1990-01-01'
        char['age'] = 50  # Incohérent avec la date de naissance

        event = {
            'id': 1,
            'title': 'Événement',
            'date': '2020-06-15',
            'characters': [1]
        }

        conflicts = self.ai.check_timeline_consistency([event], [char])

        # Doit détecter l'incohérence d'âge
        age_issues = [c for c in conflicts if c['type'] == 'age_inconsistency']
        self.assertGreater(len(age_issues), 0, "Devrait détecter incohérence d'âge")

    def test_analyze_text_fast(self):
        """Test analyse rapide de texte"""
        text = TestDataGenerator.generate_manuscript_text('mixed', length=200)

        result = self.ai.analyze_text(text, mode='fast')

        self.assertIn('entities', result)
        self.assertIn('stats', result)
        self.assertIn('word_count', result['stats'])
        self.assertIn('sentence_count', result['stats'])

        # Vérifier que les stats sont cohérentes
        self.assertGreater(result['stats']['word_count'], 0)
        self.assertGreater(result['stats']['sentence_count'], 0)

    def test_analyze_text_detailed(self):
        """Test analyse détaillée de texte"""
        text = TestDataGenerator.generate_manuscript_text('mixed', length=200)

        result = self.ai.analyze_text(text, mode='detailed')

        # Mode détaillé doit inclure plus d'infos
        self.assertIn('style', result)
        self.assertIn('structure', result)
        self.assertIn('dialogue_ratio', result)

    def test_detect_character_mentions(self):
        """Test détection mentions de personnages"""
        # Créer un personnage
        char = {
            'id': 1,
            'name': 'Jean Dupont',
            'nickname': 'JD'
        }

        # Texte contenant des mentions
        text = "Jean Dupont entra dans la pièce. JD était nerveux. Il cherchait Jean."

        mentions = self.ai.detect_character_mentions(text, [char])

        self.assertIn(1, mentions)
        # Doit compter "Jean Dupont" + "JD" + "Jean" (si on considère le prénom)
        self.assertGreater(mentions[1], 0)

    def test_analyze_text_dialogue_detection(self):
        """Test détection de dialogues"""
        text_with_dialogue = TestDataGenerator.generate_manuscript_text('dialogue', length=100)

        result = self.ai.analyze_text(text_with_dialogue, mode='detailed')

        # Texte avec dialogue devrait avoir un ratio > 0
        self.assertGreater(result['dialogue_ratio'], 0)

    def test_integration_full_story_analysis(self):
        """Test intégration complète sur une histoire"""
        data = TestDataGenerator.generate_complete_story_data()

        # Ajouter des IDs aux personnages
        for i, char in enumerate(data['characters']):
            char['id'] = i + 1

        # Analyser les relations
        relations = self.ai.analyze_character_relationships(data['characters'])
        self.assertIsInstance(relations, list)

        # Analyser chaque personnage
        for char in data['characters']:
            arc_analysis = self.ai.analyze_character_arc_coherence(char)
            self.assertIn('score', arc_analysis)

        # Vérifier la chronologie (si les événements ont des caractères valides)
        conflicts = self.ai.check_timeline_consistency(
            data['events'],
            data['characters']
        )
        self.assertIsInstance(conflicts, list)


class TestAIServicePerformance(unittest.TestCase):
    """Tests de performance du service IA"""

    def setUp(self):
        self.ai = AIService()

    def test_performance_many_characters(self):
        """Test performance avec beaucoup de personnages"""
        import time

        # Générer 50 personnages
        characters = [
            TestDataGenerator.generate_character(complete=True)
            for _ in range(50)
        ]

        for i, char in enumerate(characters):
            char['id'] = i + 1

        start = time.time()
        suggestions = self.ai.analyze_character_relationships(characters)
        elapsed = time.time() - start

        # Doit s'exécuter en moins de 2 secondes
        self.assertLess(elapsed, 2.0, f"Analyse trop lente: {elapsed}s")

    def test_performance_long_text(self):
        """Test performance sur texte long"""
        import time

        # Générer un texte de 5000 mots
        text = TestDataGenerator.generate_manuscript_text('mixed', length=5000)

        start = time.time()
        result = self.ai.analyze_text(text, mode='fast')
        elapsed = time.time() - start

        # Doit s'exécuter en moins de 3 secondes
        self.assertLess(elapsed, 3.0, f"Analyse trop lente: {elapsed}s")


if __name__ == '__main__':
    unittest.main()