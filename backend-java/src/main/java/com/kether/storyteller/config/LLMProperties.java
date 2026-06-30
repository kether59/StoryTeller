package com.kether.storyteller.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Équivalent du bloc storyteller.llm dans application.yml.
 * Mirrors Python llm_config.py → LLMConfig pydantic model.
 */
@ConfigurationProperties(prefix = "storyteller.llm")
public record LLMProperties(
        String configFile,
        String defaultProvider,
        String anthropicApiKey,
        String openaiApiKey,
        String openrouterApiKey,
        String ollamaUrl
) {
    public LLMProperties {
        configFile      = configFile      != null ? configFile      : "llm_config.json";
        defaultProvider = defaultProvider != null ? defaultProvider : "anthropic";
        anthropicApiKey = anthropicApiKey != null ? anthropicApiKey : "";
        openaiApiKey    = openaiApiKey    != null ? openaiApiKey    : "";
        openrouterApiKey = openrouterApiKey != null ? openrouterApiKey : "";
        ollamaUrl       = ollamaUrl       != null ? ollamaUrl       : "http://localhost:11434";
    }
}
