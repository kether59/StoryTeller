package com.kether.storyteller.domain.service;

/**
 * Service de domaine : extrait un extrait de texte pour servir de référence de style.
 *
 * FONCTIONNEMENT :
 * - Prend un texte complet et un nombre de mots cible
 * - Retourne les N derniers mots du texte
 * - Pure logique Java, testable instantanément
 *
 * AVANT : C'était une méthode privée dans LLMService. Maintenant c'est un
 * service réutilisable et testable.
 */
public class StyleExtractor {

    public String extractLastWords(String fullText, int targetWords) {
        if (fullText == null || fullText.isBlank()) {
            return null;
        }

        String[] words = fullText.split("\\s+");
        int startIndex = Math.max(0, words.length - targetWords);

        var result = new StringBuilder();
        for (int i = startIndex; i < words.length; i++) {
            if (result.length() > 0) result.append(" ");
            result.append(words[i]);
        }

        return result.toString().isBlank() ? null : result.toString();
    }
}