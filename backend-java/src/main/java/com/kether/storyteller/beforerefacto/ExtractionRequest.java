package com.kether.storyteller.beforerefacto;

import java.util.List;

/**
 * DTO pour la requête d'extraction.
 * Vient de la couche interface (REST controller).
 */
public record ExtractionRequest(
        Long manuscriptId,
        List<String> extractTypes  // ["characters", "locations", "timeline", "lore"]
) {
    public ExtractionRequest {
        if (manuscriptId == null || manuscriptId <= 0)
            throw new IllegalArgumentException("manuscriptId invalide");
        if (extractTypes == null || extractTypes.isEmpty())
            throw new IllegalArgumentException("extractTypes ne peut pas être vide");
    }
}
