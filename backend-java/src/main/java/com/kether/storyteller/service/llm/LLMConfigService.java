package com.kether.storyteller.service.llm;

import com.kether.storyteller.config.LLMProperties;
import com.kether.storyteller.infrastructure.web.rest.dto.Requests.LLMConfigRequest;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Gestion de la configuration LLM persistée en JSON.
 * Équivalent exact de llm_config.py :
 * <ul>
 *   <li>_load_config()  → {@link #loadConfig()}</li>
 *   <li>_save_config()  → {@link #saveConfig(LLMConfigRequest)}</li>
 *   <li>get_llm_config() → {@link #getCurrent()}</li>
 *   <li>GET /api/llm/config  → {@link #getConfigResponse()}</li>
 *   <li>POST /api/llm/config → {@link #saveConfig(LLMConfigRequest)}</li>
 * </ul>
 */
@Service
public class LLMConfigService {

    private static final Logger log = LoggerFactory.getLogger(LLMConfigService.class);

    private final LLMProperties props;
    private final ObjectMapper mapper;

    private LLMConfigModel current;   // cache en mémoire (équivalent _current_config Python)

    public LLMConfigService(LLMProperties props, ObjectMapper mapper) {
        this.props  = props;
        this.mapper = mapper;
    }

    @PostConstruct
    public void init() {
        this.current = loadConfig();
        log.info("LLM configuré : provider={} model={} ollamaUrl={}", current.getProvider(), current.getModel(), current.getOllamaUrl());
    }

    // ── Chargement ────────────────────────────────────────────────

    /** Charge depuis fichier JSON, avec fallback sur les variables d'env. */
    public LLMConfigModel loadConfig() {
        Path configPath = Path.of(props.configFile());
        if (Files.exists(configPath)) {
            LLMConfigModel cfg = mapper.readValue(configPath.toFile(), LLMConfigModel.class);
            log.debug("Config LLM chargée depuis {}", configPath);
            // Fallback clé API depuis env si absente dans le fichier
            if (cfg.getApiKey() == null || cfg.getApiKey().isBlank()) {
                cfg.setApiKey(resolveApiKeyFromEnv(cfg.getProvider()));
            }
            return cfg;
        }
        return buildFromEnv();
    }

    /** Construit la config depuis les variables d'environnement. */
    private LLMConfigModel buildFromEnv() {
        LLMConfigModel cfg = LLMConfigModel.defaults();
        String provider = props.defaultProvider();
        cfg.setProvider(provider);
        cfg.setApiKey(resolveApiKeyFromEnv(provider));
        cfg.setOllamaUrl(props.ollamaUrl());
        return cfg;
    }

    private String resolveApiKeyFromEnv(String provider) {
        return switch (provider) {
            case "anthropic"  -> props.anthropicApiKey();
            case "openai"     -> props.openaiApiKey();
            case "openrouter" -> props.openrouterApiKey();
            default           -> "";
        };
    }

    // ── Cache ─────────────────────────────────────────────────────

    /** Retourne la configuration active (équivalent get_llm_config()). */
    public LLMConfigModel getCurrent() {
        return current;
    }

    // ── Sauvegarde ────────────────────────────────────────────────

    /** Sauvegarde la configuration et met à jour le cache. */
    public LLMSaveResponse saveConfig(LLMConfigRequest req) {
        if ("openrouter".equals(req.provider())
                && (req.model() == null || req.model().isBlank())) {
            throw new IllegalArgumentException(
                    "OpenRouter requiert un nom de modèle explicite (ex: google/gemini-2.5-pro-preview)");
        }
        apply(req);
        persist();
        return new LLMSaveResponse("ok", current.getProvider(), current.getModel());
    }

    private void apply(LLMConfigRequest req) {
        if (req.provider()    != null) current.setProvider(req.provider());
        if (req.model()       != null) current.setModel(req.model());
        if (req.apiKey()      != null) current.setApiKey(req.apiKey());
        if (req.ollamaUrl()   != null) current.setOllamaUrl(req.ollamaUrl());
        if (req.temperature() != null) current.setTemperature(req.temperature());
        if (req.maxTokens()   != null) current.setMaxTokens(req.maxTokens());
    }

    private void persist() {
        Path configPath = Path.of(props.configFile());
        mapper.writerWithDefaultPrettyPrinter().writeValue(configPath.toFile(), current);
        log.info("Config LLM sauvegardée → {}", configPath);
    }

    // ── Réponses REST ─────────────────────────────────────────────

    /** GET /api/llm/config – clé API masquée. */
    public LLMConfigResponse getConfigResponse() {
        return new LLMConfigResponse(
                current.getProvider(),
                current.getModel(),
                current.maskedApiKey(),
                current.getOllamaUrl(),
                current.getTemperature(),
                current.getMaxTokens()
        );
    }

    /** GET /api/llm/health */
    public LLMHealthResponse getHealth() {
        boolean configured = current.isConfigured();
        return new LLMHealthResponse(
                configured ? "ready" : "unconfigured",
                current.getProvider(),
                current.getModel(),
                configured,
                configured
                        ? "LLM prêt à l'emploi."
                        : "Clé API manquante. Configurez le LLM dans Paramètres > LLM."
        );
    }
}