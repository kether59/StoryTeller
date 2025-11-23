from pydantic_settings import BaseSettings
from typing import List
from pathlib import Path

# 1. Définition robuste des chemins
# On récupère le dossier où se trouve ce fichier (donc .../backend)
BACKEND_DIR = Path(__file__).resolve().parent
# La racine du projet est le parent de backend
PROJECT_ROOT = BACKEND_DIR.parent

class Settings(BaseSettings):
    # La DB est forcée à la racine du projet
    database_url: str = f"sqlite:///{PROJECT_ROOT / 'storyteller.db'}"

    cors_origins: List[str] = ["*"]

    class Config:
        # Pydantic ira chercher le .env à la racine
        env_file = str(PROJECT_ROOT / ".env")
        env_file_encoding = 'utf-8'
        extra = "ignore"

settings = Settings()