package com.kether.storyteller.application.dto;

/**
 * DTO pour la réponse de validation et création.
 * Retourné par le Use Case vers le Controller.
 */
public record ValidationResult(
        String status,         // "created", "rejected", "duplicate", "error"
        String itemType,
        Object itemId,         // ID de l'élément créé ou null
        String message
) {
    /**
     * Crée un résultat de création réussie.
     */
    public static ValidationResult created(String itemType, Object itemId) {
        return new ValidationResult("created", itemType, itemId, null);
    }

    /**
     * Crée un résultat de rejet.
     */
    public static ValidationResult rejected(String itemType, String reason) {
        return new ValidationResult("rejected", itemType, null, reason);
    }

    /**
     * Crée un résultat d'erreur.
     */
    public static ValidationResult error(String itemType, String errorMessage) {
        return new ValidationResult("error", itemType, null, errorMessage);
    }

    /**
     * Crée un résultat de doublon détecté.
     */
    public static ValidationResult duplicate(String itemType, String reason) {
        return new ValidationResult("duplicate", itemType, null, reason);
    }
}
