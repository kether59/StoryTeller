package com.kether.storyteller.domain.model;

public record Manuscript(
        Long id,
        Long storyId,
        String title,
        Integer chapter,
        String text,
        String status
) {
    public boolean isTooShortForAnalysis() {
        return text == null || text.length() < 100;
    }
}