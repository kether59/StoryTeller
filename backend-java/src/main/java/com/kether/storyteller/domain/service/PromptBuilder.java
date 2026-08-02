package com.kether.storyteller.domain.service;

import com.kether.storyteller.domain.entity.Story;
import com.kether.storyteller.domain.entity.Character;
import com.kether.storyteller.domain.entity.Location;
import com.kether.storyteller.domain.entity.TimelineEvent;
import com.kether.storyteller.domain.entity.LoreEntry;

import java.util.List;

/**
 * Service de domaine : construit les prompts système pour le LLM.
 *
 * FONCTIONNEMENT :
 * - Reçoit les entités du domaine (Story, Character...)
 * - Construit un prompt texte structuré selon les règles métier
 * - Aucune dépendance externe (pas d'ObjectMapper, pas de HTTP)
 *
 * POURQUOI dans le domaine ?
 * La construction du prompt est une règle métier : "Comment parler au LLM
 * pour qu'il écrive comme un romancier". Ce n'est pas un détail technique.
 *
 * AVANT : C'était dans LLMService (500 lignes). Maintenant c'est isolé,
 * testable unitairement, et réutilisable.
 */
public class PromptBuilder {

    public String buildSystemPrompt(Story story,
                                    List<Character> characters,
                                    List<Location> locations,
                                    List<TimelineEvent> timeline,
                                    List<LoreEntry> lore,
                                    String styleReference) {
        var sb = new StringBuilder();

        sb.append("""
            Tu es un écrivain professionnel spécialisé dans le roman.
            Tu participes à l'écriture du roman intitulé :
            « %s »

            Tu n'es pas un assistant conversationnel.
            Tu n'expliques jamais tes choix.
            Tu écris directement le roman.

            ========================
            OBJECTIF
            ========================
            Ton objectif est de produire un texte qui semble avoir été écrit
            par le même auteur que les chapitres précédents.

            ========================
            COHÉRENCE
            ========================
            Respecte strictement :
            - Le synopsis : %s
            - Les personnages, leurs motivations, connaissances, personnalité
            - Les lieux et leur description
            - Le lore et la chronologie
            - Les événements déjà écrits
            """.formatted(
                story.getTitle(),
                story.getSynopsis() != null ? story.getSynopsis() : "Non défini"
        ));

        appendCharacters(sb, characters);
        appendLocations(sb, locations);
        appendLore(sb, lore);
        appendTimeline(sb, timeline);

        if (styleReference != null && !styleReference.isBlank()) {
            appendStyleReference(sb, styleReference);
        }

        sb.append("""
            ========================
            STYLE D'ÉCRITURE
            ========================
            Écris comme un véritable romancier. Naturel, fluide, immersif.
            Évite les clichés, les répétitions, les explications inutiles.
            Montre les événements au lieu de les raconter.

            ========================
            INTERDICTIONS
            ========================
            Ne jamais écrire : "Voici le chapitre", "Chapitre :", "J'espère que..."
            Ne jamais commenter le texte. Ne jamais expliquer tes choix.
            Ne jamais sortir du roman. Ne jamais produire de Markdown.

            ========================
            SORTIE
            ========================
            La réponse contient uniquement le texte du roman. Rien d'autre.
            """);

        return sb.toString();
    }

    public String buildChapterUserPrompt(Integer chapterNumber,
                                         String chapterTitle,
                                         String summary,
                                         List<Character> selectedChars,
                                         List<Location> selectedLocs,
                                         int targetWords) {
        var sb = new StringBuilder();
        sb.append("Écris un nouveau chapitre pour ce roman.\n\n");
        sb.append("Chapitre ").append(chapterNumber != null ? chapterNumber : "?");
        if (chapterTitle != null) sb.append(" - ").append(chapterTitle);
        sb.append("\n\nRésumé narratif :\n").append(summary).append("\n\n");

        if (!selectedChars.isEmpty()) {
            sb.append("Personnages clés :\n");
            selectedChars.forEach(c -> sb.append("- ").append(c.getName()).append("\n"));
            sb.append("\n");
        }

        if (!selectedLocs.isEmpty()) {
            sb.append("Lieux :\n");
            selectedLocs.forEach(l -> sb.append("- ").append(l.getName()).append("\n"));
            sb.append("\n");
        }

        sb.append("Écris environ ").append(targetWords).append(" mots.");
        return sb.toString();
    }

    public String buildContinuationUserPrompt(String manuscriptTitle,
                                              String direction,
                                              int wordCount) {
        return """
            Continue le chapitre "%s".
            Direction narrative : %s
            Écris environ %d mots supplémentaires qui s'intègrent naturellement.
            Ne répète pas ce qui a déjà été écrit.
            Commence directement la suite sans préambule.
            """.formatted(manuscriptTitle, direction, wordCount);
    }

    public String buildRewriteUserPrompt(String instruction, String text) {
        return """
            Réécris ce passage selon ces instructions :
            Instructions : %s
            Passage original :
            %s
            Fournis uniquement le texte réécrit, sans explications.
            """.formatted(instruction, text);
    }

    public String buildSuggestionUserPrompt(String currentSituation) {
        return """
            Situation actuelle dans l'histoire :
            %s
            Suggère 5 idées différentes pour la prochaine scène.
            Formate ta réponse en JSON valide avec une clé "suggestions".
            """.formatted(currentSituation);
    }

    // --- Méthodes privées d'assemblage ---

    private void appendCharacters(StringBuilder sb, List<Character> characters) {
        if (characters.isEmpty()) return;
        sb.append("\n========================\nPERSONNAGES\n========================\n\n");
        for (var c : characters) {
            sb.append("### ").append(c.getName()).append("\n");
            if (c.getRole() != null) sb.append("- Rôle: ").append(c.getRole()).append("\n");
            if (c.getAge() != null) sb.append("- Âge: ").append(c.getAge()).append(" ans\n");
            if (c.getPersonality() != null) sb.append("- Personnalité: ").append(c.getPersonality()).append("\n");
            sb.append("\n");
        }
    }

    private void appendLocations(StringBuilder sb, List<Location> locations) {
        if (locations.isEmpty()) return;
        sb.append("\n========================\nLIEUX\n========================\n\n");
        for (var l : locations) {
            sb.append("### ").append(l.getName());
            if (l.getType() != null) sb.append(" (").append(l.getType()).append(")");
            sb.append("\n");
            if (l.getSummary() != null) sb.append(l.getSummary()).append("\n");
            sb.append("\n");
        }
    }

    private void appendLore(StringBuilder sb, List<LoreEntry> lore) {
        if (lore.isEmpty()) return;
        sb.append("\n========================\nLORE ET MONDES\n========================\n\n");
        for (var e : lore) {
            sb.append("### ").append(e.getTitle());
            if (e.getCategory() != null) sb.append(" - ").append(e.getCategory());
            sb.append("\n");
            if (e.getContent() != null) sb.append(e.getContent()).append("\n");
            sb.append("\n");
        }
    }

    private void appendTimeline(StringBuilder sb, List<TimelineEvent> timeline) {
        if (timeline.isEmpty()) return;
        sb.append("\n========================\nCHRONOLOGIE\n========================\n\n");
        int limit = Math.min(timeline.size(), 10);
        for (int i = 0; i < limit; i++) {
            var ev = timeline.get(i);
            sb.append("- ").append(ev.getTitle());
            if (ev.getDate() != null) sb.append(" (").append(ev.getDate()).append(")");
            if (ev.getSummary() != null) sb.append(": ").append(ev.getSummary());
            sb.append("\n");
        }
        sb.append("\n");
    }

    private void appendStyleReference(StringBuilder sb, String styleReference) {
        sb.append("\n========================\nSTYLE DE RÉFÉRENCE\n========================\n\n");
        sb.append("Voici un extrait du chapitre précédent.\n");
        sb.append("Il représente la référence absolue pour le style.\n\n");
        sb.append(styleReference).append("\n\n");
    }
}