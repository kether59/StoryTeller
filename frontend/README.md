# StoryTeller Frontend — React 18 + Vite

**Stack** : React 18 • Vite 5 • Axios • React Router 6

---

## 🚀 Démarrage

```bash
cd frontend
npm install
npm run dev
```

URL : `http://localhost:5173`

### Build production

```bash
npm run build
npm run preview
```

---

## 🏗️ Architecture

### Organisation

```
src/
├── api/
│   └── api.js              # Axios instance + interceptors
├── components/             # Composants métier (1 par onglet)
│   ├── App.jsx             # Routing + layout principal
│   ├── ProjectSelector.jsx # Sélection du roman
│   ├── StoryPanel.jsx      # Fiche roman
│   ├── CharacterPanel.jsx  # Personnages
│   ├── LocationPanel.jsx   # Lieux
│   ├── LorePanel.jsx       # Lore
│   ├── TimelinePanel.jsx   # Chronologie
│   ├── ManuscriptPanel.jsx # Manuscrits
│   ├── WritingAssistantPanel.jsx  # IA générative
│   ├── AiPanel.jsx         # IA analytique
│   ├── ExtractionPanel.jsx # Extraction auto
│   └── LlmConfigpanel.jsx  # Config LLM
├── hooks/
│   └── useEntityCrud.js    # Hook générique CRUD (list/form/save/delete)
└── utils/
    └── notify.js           # Notifications utilisateur
```

### Pattern CRUD

Tous les panneaux de données (Character, Location, Lore, Timeline, Manuscript) utilisent `useEntityCrud` :

```javascript
const {list, form, save, del, edit, updateField, loading} = useEntityCrud({
    endpoint: '/api/characters',
    storyId: selectedStory?.id,
    initialForm: {name: '', role: '', personality: ''}
})
```

---

## 🔌 Communication Backend

L'API base URL est relative (`''`) pour fonctionner :

- En dev : via le proxy Vite (`vite.config.js`)
- En prod : via nginx reverse proxy

```javascript
// api.js
const API = axios.create({
    baseURL: import.meta.env.VITE_API_URL ?? '',
})
```

### Gestion d'erreurs

Un interceptor centralise les erreurs et ajoute `error.friendlyMessage` pour un affichage uniforme dans tous les
composants.

---

## 🤖 Fonctionnalités IA

### 1. Assistant d'écriture (`WritingAssistantPanel`)

- **Générer un chapitre** : contexte story + personnages + lieux sélectionnés
- **Continuer l'écriture** : à partir d'un manuscrit existant
- **Réécrire** : reformulation selon instruction
- **Suggérer la scène suivante** : 3 propositions de scénario

### 2. Analyse narrative (`AiPanel`)

- **Liens personnages** : détecte relations familiales/générationnelles
- **Conflits chronologiques** : incohérences de dates dans la timeline
- **Cohérence du script** : mentions de personnages/lore dans le manuscrit
- **Comportement personnages** : actions incohérentes avec la fiche
- **Vérification lore** : concepts du monde mentionnés

### 3. Extraction auto (`ExtractionPanel`)

Analyse un manuscrit pour extraire :

- Personnages (nom, rôle, âge, personnalité...)
- Lieux (nom, type, description)
- Événements chronologiques
- Éléments de lore

L'utilisateur peut valider et créer en un clic.

---

## 🎨 Styles

Styles globaux minimaux dans `styles.css`.  
Les composants utilisent des classes utilitaires :

- `.panel` — carte blanche
- `.input`, `textarea`, `select` — champs de formulaire
- `.primary` — bouton action
- `.active` — onglet actif

> **Recommandation** : migrer vers Tailwind CSS ou un vrai design system pour une UI cohérente.

---

## ⚠️ Points de vigilance

### Composants monolithiques

`ExtractionPanel` (~30KB) et `WritingAssistantPanel` (~22KB) gèrent trop de responsabilités.  
**À refactorer** en sous-composants :

```
ExtractionPanel/
├── ExtractedCharactersList.jsx
├── ExtractedLocationsList.jsx
├── ValidationToolbar.jsx
└── ExtractionResultModal.jsx
```

### Pas d'Error Boundaries

Une erreur React dans un panneau fait crasher toute l'application.  
**À ajouter** : `<ErrorBoundary>` autour de chaque onglet.

### Pas de TypeScript

Le projet utilise JSX vanilla. La migration TS améliorerait la fiabilité des props et des appels API.

---

## 🔧 Variables d'environnement

| Variable       | Description                  | Défaut         |
|----------------|------------------------------|----------------|
| `VITE_API_URL` | URL absolue de l'API backend | `''` (relatif) |

```bash
# Exemple : backend sur autre machine
VITE_API_URL=http://192.168.1.10:8000 npm run dev
```

---

## 📦 Dépendances principales

| Package                      | Usage                           |
|------------------------------|---------------------------------|
| `axios`                      | HTTP client                     |
| `react-router-dom`           | Routing (si multi-page un jour) |
| `react-markdown-editor-lite` | Éditeur markdown manuscrits     |
| `react-markdown`             | Rendu markdown                  |