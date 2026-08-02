package com.kether.storyteller.application.service;

import com.kether.storyteller.application.dto.LLMConfigDto;       // ✅ CORRIGÉ
import com.kether.storyteller.application.dto.LLMTestResultDto;   // ✅ CORRIGÉ
import com.kether.storyteller.domain.port.in.llm.ManageLLMConfigUseCase;
import com.kether.storyteller.infrastructure.llm.registry.LLMProviderRegistry;
import com.kether.storyteller.service.llm.LLMConfigModel;
import com.kether.storyteller.service.llm.LLMConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class LLMConfigApplicationService implements ManageLLMConfigUseCase {

    private static final Logger log = LoggerFactory.getLogger(LLMConfigApplicationService.class);

    private final LLMConfigService configService;
    private final LLMProviderRegistry providerRegistry;

    public LLMConfigApplicationService(LLMConfigService configService,
                                       LLMProviderRegistry providerRegistry) {
        this.configService = configService;
        this.providerRegistry = providerRegistry;
    }

    @Override
    public LLMConfigDto getCurrentConfig() {
        var cfg = configService.getCurrent();
        return new LLMConfigDto(
                cfg.getProvider(),
                cfg.getModel(),
                cfg.getApiKey(),
                cfg.getOllamaUrl(),
                cfg.getTemperature(),
                cfg.getMaxTokens()
        );
    }

    @Override
    public LLMConfigDto updateConfig(LLMConfigDto dto) {
        var cfg = configService.getCurrent();
        cfg.setProvider(dto.provider());
        cfg.setModel(dto.model());
        cfg.setApiKey(dto.apiKey());
        cfg.setOllamaUrl(dto.ollamaUrl());
        if (dto.temperature() != null) cfg.setTemperature(dto.temperature());
        if (dto.maxTokens() != null) cfg.setMaxTokens(dto.maxTokens());

        configService.save(cfg);
        return dto;
    }

    @Override
    public LLMTestResultDto testConnection(String provider, String model, String apiKey) {
        var tmpCfg = new LLMConfigModel();
        tmpCfg.setProvider(provider);
        tmpCfg.setModel(model != null && !model.isBlank() ? model : getDefaultModel(provider));
        tmpCfg.setApiKey(apiKey);

        try {
            var resolvedProvider = providerRegistry.resolve(provider);
            String response = resolvedProvider.test(tmpCfg);
            return new LLMTestResultDto(true, "✅ Connexion réussie avec " + tmpCfg.getModel());
        } catch (Exception e) {
            log.error("Test connexion échoué — provider={}", provider, e);
            return new LLMTestResultDto(false, "❌ " + e.getMessage());
        }
    }

    private String getDefaultModel(String provider) {
        return switch (provider != null ? provider.toLowerCase() : "") {
            case "ollama" -> "mistral";
            case "lmstudio" -> "local-model";
            case "anthropic" -> "claude-sonnet-4-5";
            case "openai" -> "gpt-4o";
            default -> "default";
        };
    }
}