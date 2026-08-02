package com.kether.storyteller.application.dto;

public record SuggestionCommand(
        Long storyId,
        String currentSituation
) {}