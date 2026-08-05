# StoryTeller Backend — Java 25 + Spring Boot 4

**Version** : 2.0.0  
**Architecture** : Hexagonale (Ports & Adapters)  
**Stack** : Java 25 • Spring Boot 4.1 • Maven • SQLite • Virtual Threads

---

## 🚀 Démarrage rapide

### Prérequis
- Java 25 (Temurin)
- Maven 3.9+
- (Optionnel) Docker + Docker Compose

### Développement local
```bash
cd backend-java
./mvnw spring-boot:run
```
API : `http://localhost:8000`  
Swagger UI : `http://localhost:8000/docs`

### Docker
```bash
docker compose up -d --build   # depuis la racine du projet
```

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────┐
│  Controller (REST)                      │
│  → DTO Request/Response                 │
├─────────────────────────────────────────┤
│  Application (Use Cases + Services)     │
│  → Orchestration, transactions          │
├─────────────────────────────────────────┤
│  Domain (Pur, 0 dépendance externe)     │
│  → Models, Ports (interfaces)           │
├─────────────────────────────────────────┤
│  Infrastructure (Adapters)              │
│  → JPA, Jackson, HTTP clients           │
└─────────────────────────────────────────┘
```

**Règles de dépendance** :
- `Domain` ne dépend de **rien**
- `Application` dépend de `Domain`
- `Infrastructure` dépend de `Domain`
- `Controller` dépend de `Application`

---

## 🔌 Configuration LLM

Le backend supporte 7 providers : `ollama`, `lmstudio`, `anthropic`, `openai`, `openrouter`, `gemini`, `llama`.

**Via environnement** :
```bash
export LLM_PROVIDER=anthropic
export ANTHROPIC_API_KEY=sk-ant-...
```

**Via fichier** : `backend-java/llm_config.json` (surcharge l'env)

**Test de connexion** :
```bash
curl -X POST http://localhost:8000/api/llm/test \
  -H "Content-Type: application/json" \
  -d '{"provider":"ollama","model":"mistral","llmUrl":"http://localhost:11434"}'
```

---

## 🗄️ Base de données

SQLite auto-initialisée (`ddl-auto: update`).  
Fichier par défaut : `./storyteller.db`

> **Ne pas commiter** `storyteller.db` dans Git.

---

## 🧪 Tests

```bash
./mvnw test
```

> Actuellement aucun test n'est implémenté. Priorité : tester les use cases du domaine en mockant les ports.

---

## 📁 Structure des packages

```
com.kether.storyteller
├── domain/
│   ├── model/              # Records immuables (ExtractedCharacter, TimelineConflict...)
│   ├── port/in/llm/        # Use cases interfaces (GenerateChapterUseCase...)
│   ├── port/out/llm/       # LLMGenerationPort
│   ├── port/out/persistence/ # Repository ports
│   └── service/            # PromptBuilder, StyleExtractor (pur métier)
├── application/
│   ├── dto/                # Command/Result objects
│   ├── service/            # Implémentations des use cases LLM
│   └── usecase/            # CRUD + Analysis use cases
├── infrastructure/
│   ├── config/             # Beans partagés (ObjectMapper, RestClient)
│   ├── llm/
│   │   ├── provider/       # AnthropicLLMProvider, OllamaLLMProvider...
│   │   ├── registry/       # LLMProviderRegistry
│   │   ├── parser/         # Jackson*Parser
│   │   └── LLMHttpClient.java
│   ├── persistence/jpa/    # Entities, SpringData repos, Adapters
│   └── web/rest/dto/       # Requests.java, Responses.java
└── controller/             # TODO: déplacer dans infrastructure/web/rest/
```

---

## 🛠️ Ajouter un provider LLM

1. Créer une classe implémentant `LLMProvider` dans `infrastructure/llm/provider/`
2. L'annoter `@Component`
3. Le registry l'injecte automatiquement — **aucun autre fichier à modifier**

Exemple : voir `OllamaLLMProvider.java`

---

## 📚 API Endpoints principaux

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| GET | `/api/stories` | Liste des romans |
| POST | `/api/stories` | Créer un roman |
| GET | `/api/characters?storyId=1` | Personnages d'un roman |
| GET | `/api/manuscript?storyId=1` | Manuscrits |
| POST | `/api/llm/generate-chapter` | Génération IA |
| POST | `/api/llm/continue-writing` | Continuation IA |
| POST | `/api/ai/suggest` | Analyse narrative |
| POST | `/api/extraction/analyze` | Extraction auto (personnages, lieux...) |

Documentation complète : `http://localhost:8000/docs` (SpringDoc OpenAPI)