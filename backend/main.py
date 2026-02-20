import logging
from contextlib import asynccontextmanager
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from .database import Base, engine
from .routes import stories, characters, locations, lore, timeline, manuscript, ai, llm, extraction, llm_config

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)


@asynccontextmanager
async def lifespan(app: FastAPI):
    # Startup
    logger.info("Creating database tables...")
    Base.metadata.create_all(bind=engine)
    logger.info("Database tables created successfully")
    yield
    # Shutdown
    logger.info("Shutting down...")


app = FastAPI(
    title="StoryTeller API",
    version="2.0",
    description="API pour gérer vos histoires, personnages, chronologies et manuscrits avec IA",
    lifespan=lifespan
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Inclure tous les routers
app.include_router(stories.router)
app.include_router(characters.router)
app.include_router(locations.router)
app.include_router(lore.router)
app.include_router(timeline.router)
app.include_router(manuscript.router)
app.include_router(ai.router)
app.include_router(llm.router)
app.include_router(extraction.router)
app.include_router(llm_config.router)

@app.get("/")
def root():
    return {
        "message": "StoryTeller API",
        "version": "2.0",
        "docs": "/docs",
        "redoc": "/redoc",
        "features": ["AI Analysis", "LLM Writing Assistant"]
    }


if __name__ == "__main__":
    import uvicorn

    logger.info("🚀 Starting FastAPI server...")
    logger.info("📚 API Documentation will be available at: http://127.0.0.1:8000/docs")

    uvicorn.run(
        "backend.main:app",
        host="0.0.0.0",
        port=8000,
        reload=True
    )