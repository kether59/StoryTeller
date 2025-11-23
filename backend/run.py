"""
Script de lancement du serveur StoryTeller
Localisation : ./backend/run.py
Usage: python run.py (depuis le dossier backend)
"""
import uvicorn
import sys
from pathlib import Path

# 1. Obtenir le chemin du dossier courant (backend)
current_dir = Path(__file__).resolve().parent

# 2. Obtenir la racine du projet (le dossier parent de backend)
project_root = current_dir.parent

# 3. Ajouter la racine au sys.path pour que Python puisse résoudre "backend.main"
if str(project_root) not in sys.path:
    sys.path.insert(0, str(project_root))

if __name__ == "__main__":
    print("=" * 70)
    print("🚀 DÉMARRAGE DU SERVEUR STORYTELLER")
    print(f"📂 Context root: {project_root}")
    print("=" * 70)
    print("\n📍 Endpoints disponibles:")
    print("   • API Documentation: http://127.0.0.1:8000/docs")
    print("   • Alternative docs:  http://127.0.0.1:8000/redoc")
    print("   • API Root:          http://127.0.0.1:8000")
    print("\n💡 Appuyez sur CTRL+C pour arrêter le serveur")
    print("=" * 70 + "\n")

    uvicorn.run(
        "backend.main:app", # On garde ce chemin car la racine est dans le path
        host="0.0.0.0",
        port=8000,
        reload=True,
        log_level="info",
        # Indique à uvicorn que la racine de l'app est le dossier parent
        app_dir=str(project_root)
    )