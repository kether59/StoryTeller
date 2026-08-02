package com.kether.storyteller.application.dto;

import java.util.List;

/**
 * Commande : encapsule les données nécessaires pour générer un chapitre.
 *
 * POURQUOI un DTO et pas les paramètres bruts ?
 * - Évite les méthodes avec 8 paramètres
 * - Immutable (record)
 * - Peut traverser les couches sans exposer les entités JPA
 */
public record ChapterGenerationCommand(
        Long storyId,
        Integer chapterNumber,
        String chapterTitle,
        String summary,
        List<Long> includeCharacters,
        List<Long> includeLocations,
        String length  // "court", "long", ou default
) {}