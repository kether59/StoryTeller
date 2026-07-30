package com.kether.storyteller.domain.model;

public record ExtractedCharacter(
        String name,
        String surname,
        String role,
        Integer age,
        String physicalDescription,
        String personality,
        String motivation,
        double confidence
) {
    public ExtractedCharacter {
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("Un personnage doit avoir un nom");
        if (confidence < 0 || confidence > 1)
            throw new IllegalArgumentException("Confidence invalide");
    }
}