package com.kether.storyteller.beforerefacto;

/**
 * DTO pour la requête d'analyse IA.
 * Utilisée par les contrôleurs pour analyser les données existantes.
 */
public record AIAnalysisRequest(
        String intent,          // "link_characters", "timeline_conflicts", etc.
        Long manuscriptId,      // Optionnel : pour certaines analyses
        Long storyId           // Optionnel : la story en contexte
) {
}
