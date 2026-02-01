from pydantic_settings import BaseSettings
from typing import List
from pathlib import Path


BACKEND_DIR = Path(__file__).resolve().parent
PROJECT_ROOT = BACKEND_DIR.parent

class Settings(BaseSettings):
    database_url: str = f"sqlite:///{PROJECT_ROOT / 'storyteller.db'}"

    cors_origins: List[str] = ["*"]

    class Config:

        env_file = str(PROJECT_ROOT / ".env")
        env_file_encoding = 'utf-8'
        extra = "ignore"

settings = Settings()