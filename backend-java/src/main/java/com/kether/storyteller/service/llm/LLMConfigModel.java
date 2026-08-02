package com.kether.storyteller.service.llm;

import lombok.Data;

@Data
public class LLMConfigModel {
    private String provider = "ollama";
    private String model = "mistral";
    private String apiKey = "";
    private String llmUrl = "http://localhost:11434";  // ✅ RENOMMÉ
    private double temperature = 0.7;
    private int maxTokens = 2048;

    public static LLMConfigModel defaults() {
        return new LLMConfigModel();
    }

    public boolean isConfigured() {
        return switch (provider.toLowerCase()) {
            case "anthropic", "openai", "openrouter", "gemini" -> apiKey != null && !apiKey.isBlank();
            case "ollama", "lmstudio", "llama" -> llmUrl != null && !llmUrl.isBlank();
            default -> false;
        };
    }

    public String maskedApiKey() {
        if (apiKey == null || apiKey.length() < 8) return apiKey;
        return apiKey.substring(0, 4) + "..." + apiKey.substring(apiKey.length() - 4);
    }
}