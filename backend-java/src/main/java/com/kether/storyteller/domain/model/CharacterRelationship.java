package com.kether.storyteller.domain.model;

public record CharacterRelationship(
        String character1,
        String character2,
        String type,
        String description,
        double confidence,
        String evidence
) {
    public CharacterRelationship {
        if (character1 == null || character1.isBlank())
            throw new IllegalArgumentException("Le personnage 1 ne peut pas être vide");
        if (character2 == null || character2.isBlank())
            throw new IllegalArgumentException("Le personnage 2 ne peut pas être vide");
        if (confidence < 0 || confidence > 1)
            throw new IllegalArgumentException("Confidence invalide");
    }
}
