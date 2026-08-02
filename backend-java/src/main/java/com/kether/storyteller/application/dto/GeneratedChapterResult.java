package com.kether.storyteller.application.dto;

/**
 * Résultat : encapsule le résultat d'une génération de chapitre.
 */
public record GeneratedChapterResult(
        boolean success,
        String text,
        Integer chapterNumber,
        String chapterTitle,
        int wordCount
) {}