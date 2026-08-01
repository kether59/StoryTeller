# 📖 StoryTeller

**AI-powered novel writing and management tool with character extraction, timeline management, and creative assistance.**

A full-stack application built with **Spring Boot (Java)** backend and **React** frontend.

---

## 🚀 Quick Start

### Option 1: Docker (Recommended) 🐳

**Prerequisites:**
- [Docker Desktop](https://www.docker.com/products/docker-desktop) installed

**Steps:**

```bash
# Clone the repository
git clone https://github.com/kether59/StoryTeller
cd StoryTeller

# Configure LLM (optional - see section below)
# Create backend-java/application.yml with your API keys

# Start the application
docker-compose up -d

# Access it
# Frontend: http://localhost:8080
# Backend API docs: http://localhost:8000/docs
```

Stop with:
```bash
docker-compose down
```

---

### Option 2: Local Development 💻

**Prerequisites:**
- **Backend:** Java 25+ and Maven 3.8+
- **Frontend:** Node.js 18+ with npm

#### Backend Setup (Spring Boot):

```bash
# Navigate to backend-java directory
cd backend-java

# Build the project
mvn clean install

# Run the application (default port: 8000)
mvn spring-boot:run
# API docs will be at http://localhost:8000/docs
```

#### Frontend Setup (React + Vite):

```bash
# Navigate to frontend directory (new terminal)
cd frontend

# Install dependencies
npm install

# Start development server (default port: 5173)
npm run dev
# Frontend will be at http://localhost:5173
```

---

## 🤖 LLM Configuration

### Create `backend-java/src/main/resources/application.yml` or use environment variables:

**Option 1: Anthropic Claude (Recommended)**
```yaml
app:
  llm:
    provider: anthropic
    anthropic-api-key: sk-ant-api03-xxxxxxxxxxxxx
```

**Option 2: OpenAI GPT**
```yaml
app:
  llm:
    provider: openai
    openai-api-key: sk-xxxxxxxxxxxxx
```

**Option 3: OpenRouter (Multiple Models)**
```yaml
app:
  llm:
    provider: openrouter
    openrouter-api-key: sk-or-v1-xxxxxxxxxxxxx
    openrouter-model: google/gemini-2.5-pro-preview
```

**Option 4: Ollama (Local, Free)**
```yaml
app:
  llm:
    provider: ollama
    ollama-url: http://localhost:11434
```

---

## 📁 Project Structure

### Root Directory
```
StoryTeller/
├── backend-java/             # Spring Boot backend (Java)
├── frontend/                 # React + Vite frontend
├── docker-compose.yml        # Docker Compose configuration
├── quickstart.sh            # Quick start script for Unix
├── quickstart.bat           # Quick start script for Windows
└── README.md                # This file
```

---

### Backend (Spring Boot - Java)

**Location:** `backend-java/`

```
backend-java/
├── src/
│   ├── main/
│   │   ├── java/com/kether/storyteller/
│   │   │   ├── StoryTellerApplication.java        # Spring Boot entry point
│   │   │   ├── config/                            # Configuration classes
│   │   │   │   ├── AppConfig.java
│   │   │   │   ├── JpaConfig.java
│   │   │   │   ├── LLMProperties.java             # LLM configuration
│   │   │   │   └── WebConfig.java
│   │   │   ├── controller/                        # REST API endpoints
│   │   │   │   ├── AIController.java
│   │   │   │   ├── BasicControllers.java
│   │   │   │   ├── ContentControllers.java
│   │   │   │   ├── ExtractionController.java      # Character extraction endpoints
│   │   │   │   ├── LLMController.java
│   │   │   │   └── RootController.java
│   │   │   ├── dto/                               # Data Transfer Objects
│   │   │   │   ├── request/
│   │   │   │   │   └── Requests.java
│   │   │   │   └── response/
│   │   │   │       └── Responses.java
│   │   │   ├── entity/                            # JPA Entities (Database models)
│   │   │   │   ├── Story.java
│   │   │   │   ├── StoryCharacter.java
│   │   │   │   ├── StoryLocation.java
│   │   │   │   ├── TimelineEvent.java
│   │   │   │   ├── Manuscript.java
│   │   │   │   └── LoreEntry.java
│   │   │   ├── repository/                        # Spring Data JPA repositories
│   │   │   │   ├── StoryRepository.java
│   │   │   │   ├── CharacterRepository.java
│   │   │   │   ├── LocationRepository.java
│   │   │   │   ├── TimelineEventRepository.java
│   │   │   │   ├── ManuscriptRepository.java
│   │   │   │   └── LoreEntryRepository.java
│   │   │   ├── service/                           # Business logic layer
│   │   │   │   ├── StoryService.java
│   │   │   │   ├── CharacterService.java
│   │   │   │   ├── LocationService.java
│   │   │   │   ├── TimelineService.java
│   │   │   │   ├── ManuscriptService.java
│   │   │   │   ├── LoreService.java
│   │   │   │   ├── ExtractionService.java
│   │   │   │   ├── AIService.java
│   │   │   │   ├── DatabaseInitializer.java
│   │   │   │   └── llm/
│   │   │   │       ├── LLMService.java            # LLM API integration
│   │   │   │       ├── LLMConfigService.java
│   │   │   │       ├── LLMProviders.java
│   │   │   │       ├── LLMConfigModel.java
│   │   │   │       └── NLPService.java
│   │   │   ├── exception/                         # Exception handling
│   │   │   │   ├── GlobalExceptionHandler.java
│   │   │   │   ├── ResourceNotFoundException.java
│   │   │   │   └── ServiceUnavailableException.java
│   │   │   ├── domain/                            # Domain layer (DDD pattern)
│   │   │   │   ├── model/
│   │   │   │   │   ├── Manuscript.java
│   │   │   │   │   └── ExtractedCharacter.java
│   │   │   │   ├── port/
│   │   │   │   │   └── out/
│   │   │   │   │       ├── LLMPort.java           # LLM abstraction port
│   │   │   │   │       ├── CharacterRepositoryPort.java
│   │   │   │   │       ├── ManuscriptRepositoryPort.java
│   │   │   │   │       └── CharacterExtractionParserPort.java
│   │   │   │   └── service/
│   │   │   ├── application/                       # Application layer (use cases)
│   │   │   │   ├── dto/
│   │   │   │   └── usecase/
│   │   │   │       └── ExtractCharactersUseCase.java
│   │   │   ├── infrastructure/                    # Infrastructure layer
│   │   │   │   ├── llm/
│   │   │   │   │   ├── adapter/
│   │   │   │   │   │   └── LLMServiceAdapter.java
│   │   │   │   │   └── parser/
│   │   │   │   │       └── JacksonCharacterExtractionParser.java
│   │   │   │   └── persistence/
│   │   │   │       ├── adapter/
│   │   │   │       │   ├── JpaCharacterRepositoryAdapter.java
│   │   │   │       │   └── JpaManuscriptRepositoryAdapter.java
│   │   │   │       ├── entity/
│   │   │   │       └── repository/
│   │   │   └── interfaces/                        # Interface layer
│   │   │       └── rest/
│   │   │           └── controller/
│   │   └── resources/
│   │       ├── application.yml                    # Spring Boot configuration
│   │       ├── application-test.yml               # Test configuration
│   │       ├── llm_config.json                    # LLM provider configuration
│   │       └── nlp/                               # NLP resources
│   └── test/                                       # Unit and integration tests
├── pom.xml                                         # Maven configuration
├── Dockerfile                                      # Docker build configuration
├── docker-compose.yml
└── mvnw/mvnw.cmd                                  # Maven wrapper scripts
```

**Key Technologies:**
- **Framework:** Spring Boot 4.0.1
- **ORM:** Spring Data JPA
- **Database:** SQLite (JDBC)
- **API Documentation:** SpringDoc OpenAPI (Swagger UI)
- **NLP:** Apache OpenNLP 2.4.0
- **JSON Processing:** Jackson 2.18.0
- **Build Tool:** Maven 3.8+
- **Java Version:** 25

**Architecture Pattern:** Hexagonal Architecture (Ports & Adapters)
- `domain/` - Core business logic and domain models
- `application/` - Use cases and application services
- `infrastructure/` - Implementation of ports (LLM, persistence)
- `interfaces/` - REST controllers and HTTP layer

---

### Frontend (React + Vite)

**Location:** `frontend/`

```
frontend/
├── src/
│   ├── components/                     # React components
│   │   ├── AiPanel.jsx                # AI writing assistant panel
│   │   ├── CharacterPanel.jsx         # Character management
│   │   ├── LocationPanel.jsx          # Location management
│   │   ├── StoryPanel.jsx             # Story/Project management
│   │   ├── TimelinePanel.jsx          # Timeline events
│   │   ├── ManuscriptPanel.jsx        # Manuscript content
│   │   ├── LorePanel.jsx              # Lore/worldbuilding
│   │   ├── ExtractionPanel.jsx        # Text extraction interface
│   │   ├── LlmConfigPanel.jsx         # LLM configuration
│   │   ├── WritingAssistantPanel.jsx  # Writing assistance tools
│   │   └── ProjectSelector.jsx        # Project selection
│   ├── api/
│   │   └── api.js                     # Axios HTTP client
│   ├── hooks/
│   │   └── useEntityCrud.js           # Custom hook for CRUD operations
│   ├── utils/
│   │   └── notify.js                  # Notification utilities
│   ├── App.jsx                        # Main app component
│   ├── main.jsx                       # React entry point
│   ├── styles.css                     # Global styles
│   └── README-QUICKWINS.md            # Frontend development notes
├── index.html                         # HTML template
├── package.json                       # npm dependencies
├── vite.config.js                     # Vite configuration
├── nginx.conf                         # Nginx configuration for production
├── Dockerfile                         # Docker build configuration
└── dist/                              # Built output (generated by `npm run build`)
```

**Key Technologies:**
- **Framework:** React 18.2.0
- **Build Tool:** Vite 5.0.0
- **Router:** React Router DOM 6.14.1
- **HTTP Client:** Axios 1.4.0
- **Markdown Editing:** react-markdown-editor-lite + react-markdown
- **Node Version:** 18+
- **Package Manager:** npm

**Component Hierarchy:**
- **App.jsx** - Root component with routing
- **ProjectSelector** - Project/story selection
- **Panels** - Feature-specific UI components for CRUD operations
- **API Integration** - Centralized HTTP requests via `api.js`
- **Hooks** - Reusable logic via `useEntityCrud` for entity management

---

## 🎯 Core Features

- **📚 Story Management** - Create and organize multiple novels/stories
- **👥 Character Tracking** - Build detailed character profiles and relationships
- **🗺️ Location Management** - Map your story world with detailed locations
- **📅 Timeline** - Organize story events chronologically
- **✍️ AI Writing Assistant** - Generate chapters and content with LLM context
- **🔍 Extraction** - Auto-extract characters, locations, and events from raw text
- **🧠 NLP Processing** - Natural Language Processing for text analysis
- **💾 Persistence** - SQLite database with full CRUD operations

---

## 🛠️ Development

### Backend Development (Java/Spring Boot):

```bash
cd backend-java

# Build the project
mvn clean install

# Run with hot reload
mvn spring-boot:run

# Run tests
mvn test

# Build Docker image
mvn clean package
docker build -t storyteller-backend .
```

### Frontend Development (React/Vite):

```bash
cd frontend

# Install dependencies
npm install

# Start development server with hot reload
npm run dev

# Build for production
npm run build

# Preview production build
npm run preview

# Build Docker image
docker build -f Dockerfile -t storyteller-frontend .
```

---

## 📚 API Documentation

Once the backend is running, access interactive API documentation at:
- **Swagger UI**: `http://localhost:8000/docs`
- **ReDoc**: `http://localhost:8000/redoc`

### Key Endpoints:
- `GET /api/stories` - List all stories
- `POST /api/stories` - Create a new story
- `GET /api/stories/{id}/characters` - Get story characters
- `POST /api/extract` - Extract entities from text
- `POST /api/ai/generate` - Generate content with AI
- See Swagger UI for complete API reference

---

## 🐳 Docker Setup

### Build and Run:

```bash
# Build both images
docker-compose build

# Start all services
docker-compose up -d

# View logs
docker-compose logs -f

# Stop all services
docker-compose down
```

### Services:
- **storyteller-backend**: Java Spring Boot API (port 8000)
- **storyteller-frontend**: React Vite (port 8080)
- **Database**: SQLite (embedded in backend)

---

## 🐛 Troubleshooting

### Backend (Java) Issues:

```bash
# Rebuild everything
cd backend-java
mvn clean install

# Clear Maven cache
rm -rf ~/.m2/repository/com/kether

# Check Java version
java -version  # Should be 25+

# Run tests to verify setup
mvn test
```

### Frontend (Node) Issues:

```bash
# Clear node_modules and reinstall
rm -rf frontend/node_modules package-lock.json
cd frontend
npm install

# Clear Vite cache
rm -rf frontend/dist .vite
npm run build
```

### Docker Issues:

```bash
# View detailed logs
docker-compose logs -f backend-java
docker-compose logs -f frontend

# Rebuild without cache
docker-compose down
docker-compose build --no-cache
docker-compose up -d
```

### Database Issues:

```bash
# Reset SQLite database
rm storyteller.db
# Restart backend to reinitialize
```

---

## 📝 Development Notes

- Backend uses **Hexagonal Architecture** with clear separation of concerns
- Frontend uses **component-based** architecture with custom hooks
- LLM integration is abstraction-based via ports (easy provider switching)
- Database schema is auto-initialized on startup via `DatabaseInitializer`
- CORS is enabled for local development

---

## 📝 License

open source and MIT license

---

## 🤝 Contributing

Contributions welcome! Feel free to open issues or pull requests.
