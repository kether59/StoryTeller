# 📖 StoryTeller

**AI-powered novel writing and management tool with character extraction, timeline management, and creative assistance.**

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
# Create backend/.env with your API keys

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

### Option 2: Local Development (Python + Node.js) 💻

**Prerequisites:**
- Python 3.9+ with venv
- Node.js 18+ with npm

**Backend Setup:**

```bash
# Navigate to backend directory
cd backend

# Create virtual environment
python -m venv venv

# Activate it (Linux/Mac)
source venv/bin/activate
# OR Windows
venv\Scripts\activate

# Install dependencies
pip install -r requirements.txt

# Run the backend
python run.py
# API will be at http://localhost:8000/docs
```

**Frontend Setup (new terminal):**

```bash
# Navigate to frontend directory
cd frontend

# Install dependencies
npm install

# Start development server
npm run dev
# Frontend will be at http://localhost:5173
```

---

## 🤖 LLM Configuration

### Create `backend/.env` file:

**Option 1: Anthropic Claude (Recommended)**
```env
LLM_PROVIDER=anthropic
ANTHROPIC_API_KEY=sk-ant-api03-xxxxxxxxxxxxx
```

**Option 2: OpenAI GPT**
```env
LLM_PROVIDER=openai
OPENAI_API_KEY=sk-xxxxxxxxxxxxx
```

**Option 3: OpenRouter (Multiple Models)**
```env
LLM_PROVIDER=openrouter
OPENROUTER_API_KEY=sk-or-v1-xxxxxxxxxxxxx
OPENROUTER_MODEL=google/gemini-2.5-pro-preview
```

**Option 4: Ollama (Local, Free)**
```env
LLM_PROVIDER=ollama
OLLAMA_URL=http://localhost:11434
```

See `Llm/` folder for detailed setup guides.

---

## 📁 Project Structure

```
StoryTeller/
├── backend/              # FastAPI backend
│   ├── main.py          # App entry point
│   ├── routes/          # API endpoints
│   ├── models.py        # Database models
│   ├── requirements.txt  # Python dependencies
│   └── .env            # Configuration (create this)
│
├── frontend/            # React/Vue frontend
│   ├── src/            # Source code
│   ├── package.json    # Node.js dependencies
│   └── dist/           # Built frontend
│
├── docker-compose.yml  # Docker configuration
├── Llm/               # LLM setup documentation
└── README.md          # This file
```

---

## 🎯 Features

- **📚 Story Management** - Create and organize your novels
- **👥 Character Tracking** - Build detailed character profiles
- **🗺️ Location Management** - Map your story world
- **📅 Timeline** - Organize events chronologically
- **✍️ AI Writing Assistant** - Generate chapters with context awareness
- **🔍 Extraction** - Auto-extract characters, locations, and events from text
- **💾 Export** - Save your work as Markdown or PDF

---

## 🛠️ Development

### Backend maintenance:
```bash
cd backend
source venv/bin/activate  # Activate virtual environment
python run.py             # Run with hot reload
```

### Frontend maintenance:
```bash
cd frontend
npm run dev               # Development server with hot reload
npm run build            # Build for production
```

---

## 📚 API Documentation

Once running, access interactive API docs at:
- **Swagger UI**: `http://localhost:8000/docs`
- **ReDoc**: `http://localhost:8000/redoc`

---

## 🐛 Troubleshooting

### Docker issues:
```bash
# Check logs
docker-compose logs -f

# Rebuild containers
docker-compose down
docker-compose build --no-cache
docker-compose up
```

### Python venv issues:
```bash
# Recreate venv
rm -rf backend/venv
cd backend
python -m venv venv
source venv/bin/activate
pip install -r requirements.txt
```

### Node issues:
```bash
# Clear cache and reinstall
rm -rf frontend/node_modules package-lock.json
cd frontend
npm install
```

---

## 📝 License

Your license here.

---

## 🤝 Contributing

Contributions welcome! Feel free to open issues or pull requests.

