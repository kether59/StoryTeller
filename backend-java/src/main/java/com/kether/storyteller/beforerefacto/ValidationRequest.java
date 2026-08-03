package com.kether.storyteller.beforerefacto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * DTO pour la requête de validation et création d'un élément extrait.
 * Vient de la couche interface (REST controller).
 */
public record ValidationRequest(
        @JsonProperty("itemType")
        @JsonAlias({"item_type"})
        String itemType,       // "character", "location", "timeline", "lore"

        @JsonProperty("itemData")
        @JsonAlias({"item_data"})
        Map<String, Object> itemData,

        boolean approved,

        @JsonProperty("storyId")
        @JsonAlias({"story_id"})
        Long storyId
) {
    public ValidationRequest {
        if (itemType == null || itemType.isBlank())
            throw new IllegalArgumentException("itemType ne peut pas être vide");
        if (itemData == null)
            throw new IllegalArgumentException("itemData ne peut pas être null");
    }
}
