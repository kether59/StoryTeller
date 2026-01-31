#!/bin/sh
set -e

# Path relative to the root /
DB_PATH="/backend/data/storyteller.db"

python -m spacy download fr_core_news_md
pip install openai

if [ ! -f "$DB_PATH" ]; then
    echo "🗄️ Database not found. Initializing..."
    # We run as modules so relative imports work
    python -m backend.init_db
    python -m backend.scripts.seed
else
    echo "✅ Database exists. Skipping seed."
fi

# Start uvicorn pointing to the backend package
exec uvicorn backend.main:app --host 0.0.0.0 --port 8000