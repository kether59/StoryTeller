# StoryTeller - Backend prototype

## Installation

1. Create a virtualenv and activate it (recommended):
   ```bash
   python -m venv .venv
   source .venv/bin/activate  # on Windows: .venv\Scripts\activate
   ```
2. Install requirements:
   ```bash
   pip install -r requirements.txt
   ```
3. Install spaCy model (run locally once):
    ```bash
    python -m pip install spacy
    python -m spacy download fr_core_news_md
    ```
4. Run the backend:
   ```bash
   python -m backend.app
   ```

The API will be available at http://127.0.0.1:5000/

Endpoints include:
- `GET /api/characters` - list characters
- `POST /api/characters` - create or update character
- `DELETE /api/characters` - delete (JSON body `{id: ...}`)
- Similar for `/api/world`, `/api/timeline`, `/api/story`
- `POST /api/ai/suggest` - AI heuristics
- `GET /export/markdown` - export Markdown
- `GET /export/pdf` - export PDF


## Next steps
- Create the React frontend in `/frontend` and point it to the backend API.
- Add authentication if needed.
- Add migrations (flask db init / migrate / upgrade) if desired.