package com.kether.storyteller.domain.model;

public record ExtractedLore(
        String title,
        String category,
        String content,
        double confidence
) {
    public ExtractedLore {
        if (title == null || title.isBlank())
            throw new IllegalArgumentException("Une entrée lore doit avoir un titre");
        if (confidence < 0 || confidence > 1)
            throw new IllegalArgumentException("Confidence invalide");
    }
}
