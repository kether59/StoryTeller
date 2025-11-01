"""
Service IA amélioré pour analyse narrative et suggestions
"""
from typing import List, Dict, Tuple, Optional
from datetime import datetime
import spacy
from collections import defaultdict
import re


class AIService:
    """Service d'analyse IA pour StoryTeller"""

    def __init__(self, model_name: str = 'fr_core_news_md'):
        self._nlp = None
        self.model_name = model_name

    @property
    def nlp(self):
        """Lazy loading du modèle spaCy"""
        if self._nlp is None:
            self._nlp = spacy.load(self.model_name)
        return self._nlp

    # ====================
    # ANALYSE DE PERSONNAGES
    # ====================

    def analyze_character_relationships(self, characters: List[dict]) -> List[Dict]:
        """
        Analyse les relations potentielles entre personnages

        Suggestions basées sur:
        - Nom de famille commun (famille)
        - Âges proches (amis, rivaux)
        - Occupation similaire (collègues)
        - Classe sociale (milieu social)
        """
        suggestions = []

        for i, char_a in enumerate(characters):
            for char_b in characters[i+1:]:
                # Relation familiale (même nom de famille)
                if self._check_family_name(char_a.get('name', ''), char_b.get('name', '')):
                    suggestions.append({
                        'type': 'family',
                        'character_ids': [char_a['id'], char_b['id']],
                        'characters': [char_a['name'], char_b['name']],
                        'reason': f"Même nom de famille suggérant un lien familial",
                        'confidence': 0.8
                    })

                # Âges proches (amis potentiels)
                age_diff = self._check_age_proximity(char_a.get('age'), char_b.get('age'))
                if age_diff is not None and age_diff <= 5:
                    suggestions.append({
                        'type': 'peer',
                        'character_ids': [char_a['id'], char_b['id']],
                        'characters': [char_a['name'], char_b['name']],
                        'reason': f"Âges proches ({char_a.get('age')} et {char_b.get('age')} ans)",
                        'confidence': 0.6
                    })

                # Occupation similaire
                if self._check_similar_occupation(
                        char_a.get('occupation', ''),
                        char_b.get('occupation', '')
                ):
                    suggestions.append({
                        'type': 'professional',
                        'character_ids': [char_a['id'], char_b['id']],
                        'characters': [char_a['name'], char_b['name']],
                        'reason': f"Occupations similaires: {char_a.get('occupation')}",
                        'confidence': 0.7
                    })

                # Classe sociale commune
                if char_a.get('social_class') and char_a.get('social_class') == char_b.get('social_class'):
                    suggestions.append({
                        'type': 'social',
                        'character_ids': [char_a['id'], char_b['id']],
                        'characters': [char_a['name'], char_b['name']],
                        'reason': f"Même classe sociale: {char_a.get('social_class')}",
                        'confidence': 0.5
                    })

        return suggestions

    def analyze_character_arc_coherence(self, character: dict) -> Dict:
        """
        Analyse la cohérence de l'arc narratif d'un personnage

        Vérifie:
        - Présence des éléments essentiels (désirs, peurs, conflits)
        - Cohérence backstory/personality
        - Arc de transformation clair
        """
        issues = []
        score = 100

        # Vérification des éléments essentiels
        essential_fields = {
            'desires': 'Désirs du personnage manquants',
            'fears': 'Peurs du personnage manquantes',
            'internal_conflict': 'Conflit intérieur manquant',
            'personality': 'Traits de personnalité manquants',
            'backstory': 'Histoire passée (backstory) manquante'
        }

        for field, message in essential_fields.items():
            if not character.get(field):
                issues.append({
                    'severity': 'warning',
                    'field': field,
                    'message': message
                })
                score -= 10

        # Vérification cohérence strengths/weaknesses
        if character.get('strengths') and character.get('weaknesses'):
            if self._check_text_similarity(
                    character['strengths'],
                    character['weaknesses']
            ) > 0.7:
                issues.append({
                    'severity': 'warning',
                    'field': 'strengths_weaknesses',
                    'message': 'Forces et faiblesses semblent similaires'
                })
                score -= 5

        # Vérification importance/rôle
        if character.get('importance', 5) >= 8 and character.get('role') == 'minor':
            issues.append({
                'severity': 'info',
                'field': 'role',
                'message': 'Importance élevée mais rôle mineur - incohérence potentielle'
            })

        return {
            'score': max(0, score),
            'issues': issues,
            'complete': score >= 80
        }

    def suggest_character_development(self, character: dict) -> List[Dict]:
        """
        Suggère des pistes de développement pour un personnage

        Basé sur archétypes, rôle narratif, et éléments manquants
        """
        suggestions = []

        # Suggestions basées sur l'archétype
        archetype = character.get('archetype')
        if archetype == 'hero':
            suggestions.append({
                'category': 'arc',
                'suggestion': "Envisager un voyage initiatique classique: monde ordinaire → appel → refus → mentor → épreuves → transformation"
            })
        elif archetype == 'mentor':
            suggestions.append({
                'category': 'relationship',
                'suggestion': "Développer la relation mentor/élève et le moment où le mentor laisse partir le héros"
            })

        # Suggestions si backstory manquant
        if not character.get('backstory'):
            suggestions.append({
                'category': 'development',
                'suggestion': "Créer un événement traumatisant ou formateur dans le passé qui explique les peurs et désirs actuels"
            })

        # Suggestions si conflit interne manquant
        if not character.get('internal_conflict'):
            if character.get('desires') and character.get('fears'):
                suggestions.append({
                    'category': 'conflict',
                    'suggestion': f"Créer un conflit entre le désir ({character.get('desires')[:50]}...) et la peur ({character.get('fears')[:50]}...)"
                })

        return suggestions

    # ====================
    # ANALYSE DE CHRONOLOGIE
    # ====================

    def check_timeline_consistency(
            self,
            events: List[dict],
            characters: List[dict]
    ) -> List[Dict]:
        """
        Vérifie la cohérence de la chronologie

        Détecte:
        - Personnages présents avant leur naissance
        - Personnages présents après leur mort
        - Âges incohérents
        """
        conflicts = []

        for event in events:
            if not event.get('date'):
                continue

            try:
                event_date = datetime.fromisoformat(event['date'])
            except (ValueError, TypeError):
                conflicts.append({
                    'type': 'invalid_date',
                    'event_id': event['id'],
                    'event_title': event.get('title'),
                    'message': f"Date invalide: {event.get('date')}"
                })
                continue

            # Vérifier chaque personnage impliqué
            for char_id in event.get('characters', []):
                char = next((c for c in characters if c['id'] == char_id), None)
                if not char:
                    continue

                # Vérifier la naissance
                if char.get('born'):
                    try:
                        born_date = datetime.fromisoformat(char['born'])
                        if event_date < born_date:
                            conflicts.append({
                                'type': 'born_after_event',
                                'severity': 'error',
                                'event_id': event['id'],
                                'character_id': char_id,
                                'character_name': char['name'],
                                'event_title': event.get('title'),
                                'event_date': event['date'],
                                'born_date': char['born'],
                                'message': f"{char['name']} est présent(e) à {event.get('title')} mais né(e) après"
                            })
                    except (ValueError, TypeError):
                        pass

                # Vérifier la mort
                if char.get('died'):
                    try:
                        died_date = datetime.fromisoformat(char['died'])
                        if event_date > died_date:
                            conflicts.append({
                                'type': 'present_after_death',
                                'severity': 'error',
                                'event_id': event['id'],
                                'character_id': char_id,
                                'character_name': char['name'],
                                'event_title': event.get('title'),
                                'event_date': event['date'],
                                'died_date': char['died'],
                                'message': f"{char['name']} est présent(e) à {event.get('title')} mais décédé(e) avant"
                            })
                    except (ValueError, TypeError):
                        pass

                # Vérifier l'âge cohérent
                if char.get('born') and char.get('age'):
                    try:
                        born_date = datetime.fromisoformat(char['born'])
                        age_at_event = (event_date - born_date).days // 365
                        if abs(age_at_event - char['age']) > 5:
                            conflicts.append({
                                'type': 'age_inconsistency',
                                'severity': 'warning',
                                'character_id': char_id,
                                'character_name': char['name'],
                                'message': f"Âge enregistré: {char['age']}, âge calculé à l'événement: {age_at_event}"
                            })
                    except (ValueError, TypeError):
                        pass

        return conflicts

    # ====================
    # ANALYSE DE MANUSCRIT
    # ====================

    def analyze_text(self, text: str, mode: str = 'fast') -> Dict:
        """
        Analyse un texte de manuscrit

        Mode 'fast': Entités nommées, statistiques de base
        Mode 'detailed': + Structure des phrases, sentiments, style
        """
        doc = self.nlp(text)

        # Entités nommées
        entities = [
            {
                'text': ent.text,
                'label': ent.label_,
                'start': ent.start_char,
                'end': ent.end_char
            }
            for ent in doc.ents
        ]

        # Statistiques
        stats = {
            'word_count': len([t for t in doc if not t.is_punct and not t.is_space]),
            'sentence_count': len(list(doc.sents)),
            'char_count': len(text),
            'unique_words': len(set([t.lemma_.lower() for t in doc if t.is_alpha])),
        }

        result = {
            'entities': entities,
            'stats': stats,
        }

        if mode == 'detailed':
            # Analyse stylistique
            result['style'] = self._analyze_style(doc)

            # Structure narrative
            result['structure'] = self._analyze_structure(doc)

            # Dialogues vs narration
            result['dialogue_ratio'] = self._estimate_dialogue_ratio(text)

        return result

    def detect_character_mentions(self, text: str, characters: List[dict]) -> Dict:
        """Détecte les mentions de personnages dans un texte"""
        mentions = defaultdict(int)
        text_lower = text.lower()

        for char in characters:
            name = char.get('name', '')
            if name and name.lower() in text_lower:
                mentions[char['id']] = text_lower.count(name.lower())

            nickname = char.get('nickname', '')
            if nickname and nickname.lower() in text_lower:
                mentions[char['id']] = mentions.get(char['id'], 0) + text_lower.count(nickname.lower())

        return dict(mentions)

    # ====================
    # MÉTHODES UTILITAIRES PRIVÉES
    # ====================

    def _check_family_name(self, name1: str, name2: str) -> bool:
        """Vérifie si deux noms partagent le même nom de famille"""
        if not name1 or not name2:
            return False

        parts1 = name1.strip().split()
        parts2 = name2.strip().split()

        if len(parts1) > 1 and len(parts2) > 1:
            return parts1[-1].lower() == parts2[-1].lower()
        return False

    def _check_age_proximity(self, age1: Optional[int], age2: Optional[int]) -> Optional[int]:
        """Calcule la différence d'âge"""
        if age1 is None or age2 is None:
            return None
        return abs(age1 - age2)

    def _check_similar_occupation(self, occ1: str, occ2: str) -> bool:
        """Vérifie si deux occupations sont similaires"""
        if not occ1 or not occ2:
            return False

        # Simple: vérifier si un mot clé est commun
        words1 = set(occ1.lower().split())
        words2 = set(occ2.lower().split())
        common = words1 & words2

        return len(common) > 0

    def _check_text_similarity(self, text1: str, text2: str) -> float:
        """Calcule similarité basique entre deux textes"""
        if not text1 or not text2:
            return 0.0

        doc1 = self.nlp(text1)
        doc2 = self.nlp(text2)

        return doc1.similarity(doc2)

    def _analyze_style(self, doc) -> Dict:
        """Analyse le style d'écriture"""
        # Longueur moyenne des phrases
        sentences = list(doc.sents)
        avg_sentence_length = sum(len(s) for s in sentences) / len(sentences) if sentences else 0

        # Complexité (ratio adjectifs/noms, adverbes/verbes)
        nouns = [t for t in doc if t.pos_ == 'NOUN']
        adjectives = [t for t in doc if t.pos_ == 'ADJ']
        verbs = [t for t in doc if t.pos_ == 'VERB']
        adverbs = [t for t in doc if t.pos_ == 'ADV']

        return {
            'avg_sentence_length': round(avg_sentence_length, 2),
            'adjective_ratio': round(len(adjectives) / max(1, len(nouns)), 2),
            'adverb_ratio': round(len(adverbs) / max(1, len(verbs)), 2),
        }

    def _analyze_structure(self, doc) -> Dict:
        """Analyse la structure narrative basique"""
        sentences = list(doc.sents)

        # Détection de marqueurs temporels
        temporal_markers = ['soudain', 'alors', 'puis', 'ensuite', 'finalement']
        temporal_count = sum(
            1 for sent in sentences
            if any(marker in sent.text.lower() for marker in temporal_markers)
        )

        return {
            'temporal_transitions': temporal_count,
            'paragraph_estimate': text.count('\n\n') + 1 if hasattr(doc, 'text') else 0
        }

    def _estimate_dialogue_ratio(self, text: str) -> float:
        """Estime la proportion de dialogue dans le texte"""
        # Compte les guillemets et tirets de dialogue
        dialogue_chars = text.count('«') + text.count('»') + text.count('"')
        dialogue_chars += len(re.findall(r'\n—', text)) * 20  # Estimation

        total_chars = len(text)
        return round(dialogue_chars / max(1, total_chars), 2)
