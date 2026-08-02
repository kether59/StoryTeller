package com.kether.storyteller.application.dto;

public record LLMConfigDto(
        String provider,
        String model,
        String apiKey,
        String ollamaUrl,
        Double temperature,
        Integer maxTokens
) {}