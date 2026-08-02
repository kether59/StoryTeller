package com.kether.storyteller.service.llm;

import com.kether.storyteller.config.LLMProperties;
import com.kether.storyteller.infrastructure.web.rest.dto.Requests.LLMConfigRequest;
import com.kether.storyteller.infrastructure.web.rest.dto.Responses.LLMConfigResponse;
import com.kether.storyteller.infrastructure.web.rest.dto.Responses.LLMHealthResponse;
import com.kether.storyteller.infrastructure.web.rest.dto.Responses.LLMSaveResponse;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class LLMConfigService {

    private static final Logger log = LoggerFactory.getLogger(LLMConfigService.class);

    private final LLMProperties props;
    private final ObjectMapper mapper;

    private LLMConfigModel current;

    public LLMConfigService(LLMProperties props, ObjectMapper mapper) {
        this.props = props;
        this.mapper = mapper;
    }

    @PostConstruct
    public void init() {
        this.current = loadConfig();
        log.info("LLM configuré : provider={} model={} ollamaUrl={}",
                current.getProvider(), current.getModel(), current.getOllamaUrl());
    }

    public LLMConfigModel loadConfig() {
        Path configPath = Path.of(props.configFile());
        if (Files.exists(configPath)) {
            try {
                LLMConfigModel cfg = mapper.readValue(configPath.toFile(), LLMConfigModel.class);
                log.debug("Config LLM chargée depuis {}", configPath);
                if (cfg.getApiKey() == null || cfg.getApiKey().isBlank()) {
                    cfg.setApiKey(resolveApiKeyFromEnv(cfg.getProvider()));
                }
                return cfg;
            } catch (Exception e) {
                log.error("Erreur chargement config LLM, fallback env", e);
                return buildFromEnv();
            }
        }
        return buildFromEnv();
    }

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
            case "anthropic" -> props.anthropicApiKey();
            case "openai" -> props.openaiApiKey();
            case "openrouter" -> props.openrouterApiKey();
            default -> "";
        };
    }

    public LLMConfigModel getCurrent() {
        return current;
    }

    public void save(LLMConfigModel model) {
        this.current = model;
        persist();
    }

    public LLMSaveResponse saveConfig(LLMConfigRequest req) {
        if ("openrouter".equals(req.provider())
                && (req.model() == null || req.model().isBlank())) {
            throw new IllegalArgumentException(
                    "OpenRouter requiert un nom de modèle explicite");
        }
        apply(req);
        persist();
        return new LLMSaveResponse("ok", current.getProvider(), current.getModel());
    }

    private void apply(LLMConfigRequest req) {
        if (req.provider() != null) current.setProvider(req.provider());
        if (req.model() != null) current.setModel(req.model());
        if (req.apiKey() != null) current.setApiKey(req.apiKey());
        if (req.ollamaUrl() != null) current.setOllamaUrl(req.ollamaUrl());
        if (req.temperature() != null) current.setTemperature(req.temperature());
        if (req.maxTokens() != null) current.setMaxTokens(req.maxTokens());
    }

    private void persist() {
        try {
            Path configPath = Path.of(props.configFile());
            mapper.writerWithDefaultPrettyPrinter().writeValue(configPath.toFile(), current);
            log.info("Config LLM sauvegardée → {}", configPath);
        } catch (Exception e) {
            log.error("Erreur persistance config LLM", e);
            throw new RuntimeException("Impossible de sauvegarder la config LLM", e);
        }
    }

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