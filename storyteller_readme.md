# StoryTeller - Installation et guide d'utilisation

## 1. Installation du projet

### Prérequis
- Python 3.10+ ou 3.11
- Node.js 18+ et npm
- Git (optionnel)

### Backend (Flask + SQLite)
1. Cloner le dépôt ou télécharger les fichiers.
2. Créer un environnement virtuel et l'activer :
```bash
python -m venv .venv
source .venv/bin/activate  # Windows : .venv\Scripts\activate
```
3. Installer les dépendances :
```bash
pip install -r requirements.txt
```
4. Installer le modèle spaCy français :
```bash
python -m spacy download fr_core_news_md
```
5. Initialiser la base de données et appliquer les migrations :
```bash
export FLASK_APP=backend.manage:app  # Windows : set FLASK_APP=backend.manage:app
flask db init
flask db migrate -m "initial"
flask db upgrade
```
6. (Optionnel) Ajouter des données de test :
```bash
python -m backend.scripts.seed
```
7. Lancer le serveur backend :
```bash
python -m backend.app
```
Le backend sera accessible sur `http://127.0.0.1:5000/`.

### Frontend (React + Vite)
1. Se placer dans le dossier `frontend/` :
```bash
cd frontend
```
2. Installer les dépendances :
```bash
npm install
```
3. Lancer l'application React :
```bash
npm run dev
```
4. Ouvrir le navigateur sur l'URL affichée par Vite (ex: `http://localhost:5173/`).

---

## 2. Utilisation

### 2.1 Navigation
- Barre de navigation en haut de la page pour passer entre :
  - Personnages
  - Monde
  - Chronologie
  - Histoire
  - Assistant IA

### 2.2 Gestion des Personnages
- Ajouter / éditer : remplir les champs et cliquer sur `Enregistrer`
- Supprimer : cliquer sur `Supprimer`

### 2.3 Gestion du Monde
- Ajouter / éditer : remplir les champs `Titre`, `Type`, `Résumé` puis `Enregistrer`
- Supprimer : cliquer sur `Supprimer`

### 2.4 Chronologie
- Ajouter / éditer un événement avec date et résumé
- Cocher les personnages impliqués
- Sauvegarder
- Supprimer un événement via `Supprimer`

### 2.5 Histoire / Manuscrit
- Modifier le texte, titre, synopsis et quatrième de couverture
- Sauvegarde automatique toutes les 5 secondes (ou bouton `Enregistrer`)
- Export Markdown ou PDF avec annotations

### 2.6 Assistant IA
- Trouver liens personnages (suggère relations basées sur nom / âge)
- Vérifier la chronologie (détecte incohérences dates de naissance / événements)

### 2.7 Éditeur Manuscrit Markdown
- Interface avec éditeur Markdown enrichi
- Analyse rapide ou détaillée via l'IA intégrée
- Liste des chapitres, création et suppression
- Exporte le manuscrit annoté pour les incohérences détectées

---

## 3. Copies d'écran (exemple)

### Navigation principale
```
+----------------------------+
| storyteller                 |
| Personnages Monde Chrono..|
+----------------------------+
```

### Liste des Personnages
```
Jean Dupont — 45 ans [Éditer] [Supprimer]
Marie Dupont — 42 ans [Éditer] [Supprimer]
```

### Édition d'un chapitre manuscrit
```
Titre: Chapitre 1
[Éditeur Markdown enrichi]
Enregistrer  Analyser rapide  Analyser détaillé  Export Markdown  Export PDF
```

### Assistant IA
```
[Trouver liens personnages] [Vérifier chronologie]
{JSON output des suggestions et incohérences}
```

---

## 4. Notes et bonnes pratiques
- Toujours lancer le backend avant le frontend
- Les exports PDF et Markdown nécessitent que le backend soit actif
- Pour la production : sécuriser l'API, gérer les utilisateurs et l'authentification
- Sauvegardes régulières de `storyteller.db` recommandées

