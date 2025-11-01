# 📖 StoryTeller - Outil d'écriture

## 📦 Installation 

### Backend

```bash
# Créer l'environnement virtuel
python -m venv .venv
source .venv/bin/activate  # Windows: .venv\Scripts\activate

# Installer les dépendances (nouvelles bibliothèques ajoutées)
pip install -r requirements.txt

# Installer spaCy avec le modèle français
python -m spacy download fr_core_news_md

# Variables d'environnement (créer un fichier .env)
export FLASK_ENV=development
export SECRET_KEY=votre-cle-secrete
export DATABASE_URL=sqlite:///storyteller.db

# Initialiser la base de données avec les nouveaux modèles
flask db init
flask db migrate -m "Enhanced character model"
flask db upgrade

# Lancer le serveur
python -m backend.app
```

### Frontend

```bash
cd frontend
npm install
npm run dev
```

---

## 🧪 Tests

### Exécuter les tests

```bash
# Tests unitaires
python -m pytest backend/tests/ -v

# Tests avec couverture
python -m pytest backend/tests/ --cov=backend --cov-report=html

# Tests du service IA uniquement
python -m pytest backend/tests/test_ai_service.py -v

# Tests de performance
python -m pytest backend/tests/test_ai_service.py::TestAIServicePerformance -v
```

### Générer des données de test

```python
from backend.tests.test_data import TestDataGenerator

# Générer un personnage complet
character = TestDataGenerator.generate_character(complete=True)

# Générer une famille
family = TestDataGenerator.generate_family(size=4)

# Générer un jeu de données complet
story_data = TestDataGenerator.generate_complete_story_data()
```

---

## 🔍 Utilisation du Service IA

### 1. Analyser les relations entre personnages

```python
from backend.services.ai_service import AIService

ai = AIService()

# Personnages avec des liens potentiels
characters = [
    {'id': 1, 'name': 'Jean Dupont', 'age': 45, 'occupation': 'médecin'},
    {'id': 2, 'name': 'Marie Dupont', 'age': 42, 'occupation': 'avocate'},
    {'id': 3, 'name': 'Thomas Martin', 'age': 44, 'occupation': 'médecin'}
]

suggestions = ai.analyze_character_relationships(characters)

# Résultat:
# [
#   {
#     'type': 'family',
#     'character_ids': [1, 2],
#     'reason': 'Même nom de famille suggérant un lien familial',
#     'confidence': 0.8
#   },
#   {
#     'type': 'professional',
#     'character_ids': [1, 3],
#     'reason': 'Occupations similaires: médecin',
#     'confidence': 0.7
#   }
# ]
```

### 2. Vérifier la cohérence d'un arc narratif

```python
character = {
    'name': 'Jean Dupont',
    'desires': 'Obtenir justice pour sa famille',
    'fears': 'Perdre ses proches',
    'internal_conflict': 'Vengeance vs pardon',
    'personality': 'Déterminé mais impulsif',
    # ... autres champs
}

analysis = ai.analyze_character_arc_coherence(character)

# Résultat:
# {
#   'score': 85,  # Score de cohérence
#   'issues': [   # Problèmes détectés
#     {
#       'severity': 'warning',
#       'field': 'backstory',
#       'message': 'Histoire passée (backstory) manquante'
#     }
#   ],
#   'complete': True
# }
```

### 3. Détecter les incohérences temporelles

```python
characters = [
    {'id': 1, 'name': 'Jean', 'born': '1990-01-01', 'died': '2050-12-31'}
]

events = [
    {
        'id': 1,
        'title': 'Bataille finale',
        'date': '2055-06-15',  # Après la mort de Jean
        'characters': [1]
    }
]

conflicts = ai.check_timeline_consistency(events, characters)

# Résultat:
# [
#   {
#     'type': 'present_after_death',
#     'severity': 'error',
#     'character_name': 'Jean',
#     'message': 'Jean est présent(e) à Bataille finale mais décédé(e) avant'
#   }
# ]
```

### 4. Analyser un texte de manuscrit

```python
text = """
Jean Dupont entra dans la pièce sombre. Marie l'attendait depuis des heures.
— Il faut partir, dit-elle d'une voix tremblante.
— Je sais, répondit-il.
"""

# Analyse rapide
result = ai.analyze_text(text, mode='fast')
# {
#   'entities': [...],  # Entités nommées détectées
#   'stats': {
#     'word_count': 25,
#     'sentence_count': 4,
#     'unique_words': 20
#   }
# }

# Analyse détaillée
result = ai.analyze_text(text, mode='detailed')
# + 'style', 'structure', 'dialogue_ratio'
```

---



## 📚 Ressources et Documentation

### Backend
- [Flask Documentation](https://flask.palletsprojects.com/)
- [SQLAlchemy ORM](https://docs.sqlalchemy.org/)
- [Marshmallow Validation](https://marshmallow.readthedocs.io/)
- [spaCy NLP](https://spacy.io/)

### Frontend
- [React Documentation](https://react.dev/)
- [Vite Build Tool](https://vitejs.dev/)
- [Axios HTTP Client](https://axios-http.com/)

### Tests
- [pytest Documentation](https://docs.pytest.org/)
- [unittest (Python)](https://docs.python.org/3/library/unittest.html)

---

## 🐛 Résolution de Problèmes

### Erreur: spaCy model not found
```bash
python -m spacy download fr_core_news_md
```

### Erreur: Base de données non initialisée
```bash
flask db init
flask db migrate
flask db upgrade
```

### Erreur CORS sur le frontend
Vérifier que `CORS_ORIGINS` dans `config.py` inclut l'URL du frontend.

### Tests qui échouent
```bash
# Vérifier l'environnement de test
export FLASK_ENV=testing
python -m pytest -v
```

---


## 👥 Contribution

Les contributions sont les bienvenues ! Pour contribuer :

1. Fork le projet
2. Créer une branche (`git checkout -b feature/amélioration`)
3. Commit les changements (`git commit -m 'Ajout fonctionnalité X'`)
4. Push vers la branche (`git push origin feature/amélioration`)
5. Ouvrir une Pull Request

### Guidelines
- Ajouter des tests pour toute nouvelle fonctionnalité
- Suivre les conventions de code (PEP 8 pour Python)
- Documenter les nouvelles fonctions/classes
- Mettre à jour le README si nécessaire

---

## 📄 Licence

MIT License - Voir le fichier LICENSE pour plus de détails.

---

## 💡 Support

Pour toute question ou problème :
- Ouvrir une issue sur GitHub
- Consulter la documentation
- Rejoindre la communauté (Discord/Slack si applicable)