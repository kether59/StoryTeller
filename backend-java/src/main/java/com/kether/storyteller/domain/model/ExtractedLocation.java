package com.kether.storyteller.domain.model;

public record ExtractedLocation(
        String name,
        String type,
        String summary,
        double confidence
) {
    public ExtractedLocation {
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("Un lieu doit avoir un nom");
        if (confidence < 0 || confidence > 1)
            throw new IllegalArgumentException("Confidence invalide");
    }
}
