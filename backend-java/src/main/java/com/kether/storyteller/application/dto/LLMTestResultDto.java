package com.kether.storyteller.application.dto;

public record LLMTestResultDto(
        boolean success,
        String message
) {}