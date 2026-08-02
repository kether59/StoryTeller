package com.kether.storyteller.beforerefacto;

import java.util.Map;

/**
 * DTO pour la requête de validation et création d'un élément extrait.
 * Vient de la couche interface (REST controller).
 */
public record ValidationRequest(
        String itemType,       // "character", "location", "timeline", "lore"
        Map<String, Object> itemData,
        boolean approved,
        Long storyId
) {
    public ValidationRequest {
        if (itemType == null || itemType.isBlank())
            throw new IllegalArgumentException("itemType ne peut pas être vide");
        if (itemData == null)
            throw new IllegalArgumentException("itemData ne peut pas être null");
    }
}
