package com.kether.storyteller.application.dto;

import com.kether.storyteller.domain.model.*;
import java.util.List;

/**
 * DTO pour la réponse d'extraction combinée.
 * Retourné par le Controller vers le client REST.
 */
public record ExtractionResponse(
        List<ExtractedCharacter> characters,
        List<ExtractedLocation> locations,
        List<ExtractedTimelineEvent> timeline,
        List<ExtractedLore> lore,
        String rawResponse
) {
}
