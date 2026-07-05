# 📖 StoryTeller — Java Edition

**Outil complet de gestion et d'écriture de romans assisté par IA**  
**Backend : Java 25 • Spring Boot 4.1.0 • Maven**  
**Frontend : React + Vite**

---

## 🚀 Démarrage rapide

### Option 1 : Docker (recommandé)

```bash
# 1. Cloner le projet
git clone <votre-repo>
cd StoryTeller

# 2. Lancer l'application
docker compose up -d --build
```

**Accès :**
- **Frontend** → http://localhost:8080
- **Backend API** → http://localhost:8000
- **Swagger** → http://localhost:8000/swagger-ui.html

### Option 2 : Développement local

#### Backend (Java)

```bash
cd backend
./mvnw spring-boot:run
# ou
./run.sh          # script de démarrage pratique
```

#### Frontend (React)

```bash
cd frontend
npm install
npm run dev
```

---

## 🧠 Configuration LLM

Créez un fichier `backend/llm_config.json` ou utilisez les variables d'environnement.

### Exemples de configuration

**Anthropic Claude (recommandé pour l'écriture créative)**
```json
{
  "provider": "anthropic",
  "model": "claude-sonnet-4-5",
  "api_key": "sk-ant-..."
}
```

**Ollama (local, gratuit)**
```json
{
  "provider": "ollama",
  "model": "mistral",
  "ollama_url": "http://localhost:11434"
}
```

**OpenAI / OpenRouter / Gemini / LM Studio** également supportés.

---

## ✨ Fonctionnalités

| Module                    | Description |
|--------------------------|-----------|
| **📖 Histoire**          | Synopsis, blurb, export JSON/Markdown |
| **👥 Personnages**       | Fiches détaillées + arcs narratifs |
| **📍 Lieux**             | Gestion du world-building |
| **📚 Lore**              | Éléments de l'univers |
| **⏰ Chronologie**       | Événements avec personnages et lieux |
| **✍️ Manuscrit**         | Éditeur Markdown avancé + analyse NLP |
| **🤖 Assistant d'écriture** | Génération, continuation, réécriture, suggestions |
| **🔍 Extraction IA**     | Extraction automatique (personnages, lieux, timeline, lore) |
| **🧠 Analyse IA**        | Cohérence, conflits chronologiques, liens personnages |

---

## 🛠️ Stack Technique

### Backend
- **Java 25**
- **Spring Boot 4.1.0**
- **Spring Data JPA + Hibernate**
- **SQLite** (base de données embarquée)
- **OpenNLP** (analyse textuelle)
- **Virtual Threads** (concurrence moderne)
- **LLM Providers** : Anthropic, OpenAI, OpenRouter, Ollama, Gemini, LM Studio

### Frontend
- **React 18 + Vite**
- **React Markdown Editor**
- **Axios**
- **Tailwind / CSS moderne**

### Déploiement
- **Docker Compose** (multi-conteneurs)
- **Maven** pour le build Java
- **Nginx** pour servir le frontend en production

---

## 📁 Structure du projet

```
StoryTeller/
├── backend/                  # Java Spring Boot
│   ├── src/main/java/com/kether/storyteller/
│   ├── pom.xml
│   ├── run.sh
│   └── llm_config.json
│
├── frontend/                 # React
│   ├── src/
│   ├── package.json
│   └── vite.config.js
│
├── docker-compose.yml
├── README.md
└── data/                     # Base SQLite + modèles LLM
```

---

## 🧪 Commandes utiles

```bash
# Backend
./run.sh dev          # Mode développement
./run.sh build        # Compiler le JAR
./run.sh prod         # Lancer le JAR en production

# Frontend
npm run dev
npm run build

# Docker
docker compose up -d
docker compose logs -f
```

---

## 📝 Notes importantes

- La base de données est persistée dans `./data/storyteller.db`
- Les modèles OpenNLP (pour l'analyse locale) sont optionnels
- L’assistant d’écriture fonctionne même avec Ollama en local (zéro coût)

---

**Prêt à écrire votre prochain chef-d’œuvre ?** ✍️

---

**Auteur** : Kether  
**Version** : 2.0.0 (Java Migration)  
**Date** : Juillet 2026
```
