"""
llm_config.py — Gestion de la configuration LLM
Endpoints :
  GET  /api/llm/config        → lire la config courante
  POST /api/llm/config        → sauvegarder la config
  POST /api/llm/test          → tester la connexion au LLM choisi
  GET  /api/llm/health        → statut rapide (utilisé par WritingAssistantPanel)
"""
import json
import logging
import os
from pathlib import Path
from typing import Optional, Literal

from fastapi import APIRouter, HTTPException
from pydantic import BaseModel, Field

logger = logging.getLogger(__name__)
router = APIRouter(prefix="/api/llm", tags=["llm-config"])

_CONFIG_PATH = Path(os.getenv("LLM_CONFIG_PATH", "llm_config.json"))


class LLMConfig(BaseModel):
    provider: Literal["anthropic", "openai", "openrouter", "ollama"] = "anthropic"
    model: str = "claude-sonnet-4-5"
    api_key: Optional[str] = None          # Anthropic / OpenAI / OpenRouter
    ollama_url: str = "http://localhost:11434"
    temperature: float = Field(default=0.7, ge=0.0, le=2.0)
    max_tokens: int = Field(default=4000, ge=256, le=32768)

    class Config:

        extra = "ignore"


class TestRequest(BaseModel):
    provider: Literal["anthropic", "openai", "openrouter", "ollama"]
    model: str
    api_key: Optional[str] = None
    ollama_url: str = "http://localhost:11434"


def _load_config() -> LLMConfig:
    """Charge la config depuis le fichier JSON, avec fallback sur les env vars."""
    if _CONFIG_PATH.exists():
        try:
            data = json.loads(_CONFIG_PATH.read_text(encoding="utf-8"))
            return LLMConfig(**data)
        except Exception as e:
            logger.warning(f"Fichier de config corrompu, utilisation des valeurs par défaut: {e}")


    provider = os.getenv("LLM_PROVIDER", "anthropic")
    return LLMConfig(
        provider=provider,
        model=_default_model_for(provider),
        api_key=(
                os.getenv("ANTHROPIC_API_KEY") or
                os.getenv("OPENAI_API_KEY") or
                os.getenv("OPENROUTER_API_KEY") or
                None
        ),
        ollama_url=os.getenv("OLLAMA_URL", "http://localhost:11434"),
    )


def _save_config(cfg: LLMConfig) -> None:
    """Sauvegarde la config dans le fichier JSON (sans la clé API en clair si env var)."""
    _CONFIG_PATH.write_text(
        json.dumps(cfg.model_dump(), indent=2, ensure_ascii=False),
        encoding="utf-8"
    )
    logger.info(f"Config LLM sauvegardée → {_CONFIG_PATH}")


def _default_model_for(provider: str) -> str:
    defaults = {
        "anthropic": "claude-sonnet-4-5",
        "openai": "gpt-4o",
        "openrouter": "meta-llama/llama-3.1-8b-instruct:free",
        "ollama": "mistral",
    }
    return defaults.get(provider, "")


_current_config: Optional[LLMConfig] = None

def get_llm_config() -> LLMConfig:
    """Retourne la configuration active (chargée en cache)."""
    global _current_config
    if _current_config is None:
        _current_config = _load_config()
    return _current_config


@router.get("/config")
def read_config() -> LLMConfig:
    """Retourne la configuration courante."""
    cfg = get_llm_config()

    if cfg.api_key and len(cfg.api_key) > 8:
        masked = cfg.api_key[:6] + "…" + cfg.api_key[-4:]
        return cfg.model_copy(update={"api_key": masked})
    return cfg


@router.post("/config")
def save_config(new_cfg: LLMConfig):
    """Sauvegarde la configuration."""
    global _current_config
    _save_config(new_cfg)
    _current_config = new_cfg
    return {"status": "ok", "provider": new_cfg.provider, "model": new_cfg.model}


@router.post("/test")
async def test_connection(req: TestRequest):
    """
    Envoie un message minimal au LLM pour valider la clé / URL.
    Retourne {"ok": true, "message": "..."} ou lève une HTTPException.
    """
    TEST_PROMPT = "Réponds uniquement avec le mot 'OK'."

    try:
        if req.provider == "anthropic":
            _require_key(req.api_key, "ANTHROPIC_API_KEY")
            try:
                import anthropic
            except ImportError:
                raise HTTPException(503, "Module 'anthropic' non installé. Lancez : pip install anthropic")
            client = anthropic.Anthropic(api_key=req.api_key)
            resp = client.messages.create(
                model=req.model,
                max_tokens=16,
                messages=[{"role": "user", "content": TEST_PROMPT}]
            )
            text = resp.content[0].text.strip()

        elif req.provider == "openai":
            _require_key(req.api_key, "OPENAI_API_KEY")
            try:
                import openai as oai
            except ImportError:
                raise HTTPException(503, "Module 'openai' non installé. Lancez : pip install openai")
            client = oai.OpenAI(api_key=req.api_key)
            resp = client.chat.completions.create(
                model=req.model,
                max_tokens=16,
                messages=[{"role": "user", "content": TEST_PROMPT}]
            )
            text = resp.choices[0].message.content.strip()

        elif req.provider == "openrouter":
            _require_key(req.api_key, "OPENROUTER_API_KEY")
            try:
                import openai as oai
            except ImportError:
                raise HTTPException(503, "Module 'openai' non installé.")
            client = oai.OpenAI(base_url="https://openrouter.ai/api/v1", api_key=req.api_key)
            resp = client.chat.completions.create(
                model=req.model,
                max_tokens=16,
                messages=[{"role": "user", "content": TEST_PROMPT}]
            )
            text = resp.choices[0].message.content.strip()

        elif req.provider == "ollama":
            try:
                import httpx
            except ImportError:
                raise HTTPException(503, "Module 'httpx' non installé. Lancez : pip install httpx")
            async with httpx.AsyncClient(timeout=15.0) as client:
                r = await client.post(
                    f"{req.ollama_url}/api/generate",
                    json={"model": req.model, "prompt": TEST_PROMPT, "stream": False}
                )
                r.raise_for_status()
                text = r.json().get("response", "").strip()

        else:
            raise HTTPException(400, f"Provider inconnu : {req.provider}")

        return {"ok": True, "message": f"Connexion réussie. Réponse : « {text} »"}

    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"Erreur test LLM ({req.provider}): {e}")
        raise HTTPException(422, f"Erreur de connexion : {e}")


@router.get("/health")
def health():
    """
    Statut rapide utilisé par WritingAssistantPanel.
    Retourne le provider + modèle actifs sans exposer la clé.
    """
    cfg = get_llm_config()
    configured = bool(
        cfg.api_key and len(cfg.api_key) > 8
        if cfg.provider != "ollama"
        else cfg.ollama_url
    )
    return {
        "status": "ready" if configured else "unconfigured",
        "provider": cfg.provider,
        "model": cfg.model,
        "configured": configured,
        "message": (
            "LLM prêt à l'emploi." if configured
            else "Clé API manquante. Configurez le LLM dans Paramètres > LLM."
        )
    }

def _require_key(key: Optional[str], env_name: str):
    """Lève une exception si la clé API est absente."""
    if not key or not key.strip():
        env_val = os.getenv(env_name, "")
        if not env_val:
            raise HTTPException(
                422,
                f"Clé API manquante. Renseignez-la dans la configuration ou définissez la variable d'environnement {env_name}."
            )


async def call_llm_configured(system_prompt: str, user_prompt: str, max_tokens: Optional[int] = None) -> str:
    """
    Appelle le LLM avec la configuration active.
    Remplace la fonction call_llm() hard-codée dans extraction.py.

    Usage :
        from .llm_config import call_llm_configured
        text = await call_llm_configured(system_prompt, user_prompt)
    """
    cfg = get_llm_config()
    tokens = max_tokens or cfg.max_tokens

    if cfg.provider == "anthropic":
        _require_key(cfg.api_key, "ANTHROPIC_API_KEY")
        try:
            import anthropic
        except ImportError:
            raise HTTPException(503, "Module 'anthropic' non installé.")
        client = anthropic.Anthropic(api_key=cfg.api_key or os.getenv("ANTHROPIC_API_KEY"))
        msg = client.messages.create(
            model=cfg.model,
            max_tokens=tokens,
            system=system_prompt,
            messages=[{"role": "user", "content": user_prompt}]
        )
        return msg.content[0].text

    elif cfg.provider in ("openai", "openrouter"):
        base_url = "https://openrouter.ai/api/v1" if cfg.provider == "openrouter" else None
        key = cfg.api_key or os.getenv(
            "OPENROUTER_API_KEY" if cfg.provider == "openrouter" else "OPENAI_API_KEY"
        )
        _require_key(key, "OPENAI_API_KEY")
        try:
            import openai as oai
        except ImportError:
            raise HTTPException(503, "Module 'openai' non installé.")
        kwargs = {"api_key": key}
        if base_url:
            kwargs["base_url"] = base_url
        client = oai.OpenAI(**kwargs)
        resp = client.chat.completions.create(
            model=cfg.model,
            max_tokens=tokens,
            temperature=cfg.temperature,
            messages=[
                {"role": "system", "content": system_prompt},
                {"role": "user", "content": user_prompt},
            ]
        )
        return resp.choices[0].message.content

    elif cfg.provider == "ollama":
        try:
            import httpx
        except ImportError:
            raise HTTPException(503, "Module 'httpx' non installé.")
        import asyncio
        async with httpx.AsyncClient(timeout=300.0) as client:
            r = await client.post(
                f"{cfg.ollama_url}/api/generate",
                json={
                    "model": cfg.model,
                    "prompt": f"{system_prompt}\n\nUser: {user_prompt}\n\nAssistant:",
                    "stream": False,
                    "options": {"temperature": cfg.temperature, "num_predict": tokens}
                }
            )
            r.raise_for_status()
            return r.json().get("response", "")

    raise HTTPException(400, f"Provider inconnu : {cfg.provider}")