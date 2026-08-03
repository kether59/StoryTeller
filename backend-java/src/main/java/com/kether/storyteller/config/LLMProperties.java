package com.kether.storyteller.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "storyteller.llm")
public class LLMProperties {
    private String defaultProvider = "ollama";
    private String configFile = "llm_config.json";
    private String anthropicApiKey = "";
    private String openaiApiKey = "";
    private String openrouterApiKey = "";
    private String geminiApiKey = "";
    private String llmUrl = "http://localhost:11434";
}