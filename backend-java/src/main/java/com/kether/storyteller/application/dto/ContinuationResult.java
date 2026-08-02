package com.kether.storyteller.application.dto;

public record ContinuationResult(
        boolean success,
        String text,
        int wordCount
) {}