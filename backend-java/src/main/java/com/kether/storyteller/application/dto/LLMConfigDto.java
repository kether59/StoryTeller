package com.kether.storyteller.application.dto;

public record LLMConfigDto(
        String provider,
        String model,
        String apiKey,
        String llmUrl,
        Double temperature,
        Integer maxTokens
) {}