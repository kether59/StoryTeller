package com.kether.storyteller.application.dto;

public record SuggestionCommand
        (
                String storyId,
                String content,
                String userId)
{}
