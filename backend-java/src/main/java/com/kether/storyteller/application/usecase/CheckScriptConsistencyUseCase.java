package com.kether.storyteller.application.usecase;

import com.kether.storyteller.domain.port.out.LLMPort;
import com.kether.storyteller.repository.CharacterRepository;
import com.kether.storyteller.repository.LoreEntryRepository;
import com.kether.storyteller.repository.ManuscriptRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Use Case : Vérifier la cohérence du script.
 * Analyse les mentions de personnages et du lore dans le texte.
 */
@Component
@RequiredArgsConstructor
public class CheckScriptConsistencyUseCase {

    private static final Logger log = LoggerFactory.getLogger(CheckScriptConsistencyUseCase.class);

    private final CharacterRepository characterRepo;
    private final LoreEntryRepository loreRepo;
    private final ManuscriptRepository manuscriptRepo;
    private final LLMPort llmPort;

    public Map<String, Object> execute(Long storyId, Long manuscriptId) {
        // 1. Récupérer le manuscrit
        String manuscriptText = manuscriptId != null
            ? manuscriptRepo.findById(manuscriptId)
                .map(m -> m.getText())
                .orElse("")
            : "";

        // 2. Récupérer les caractères et lore
        var characters = characterRepo.findByStoryId(storyId);
        var loreEntries = loreRepo.findByStoryId(storyId);

        String charList = characters.stream()
                .map(c -> c.getName())
                .filter(n -> n != null && !n.isBlank())
                .reduce((a, b) -> a + ", " + b)
                .orElse("");

        String loreList = loreEntries.stream()
                .filter(e -> e.getTitle() != null)
                .map(e -> e.getTitle() + (e.getCategory() != null ? " (" + e.getCategory() + ")" : ""))
                .reduce((a, b) -> a + ", " + b)
                .orElse("");

        // 3. Appeler le LLM
        String systemPrompt = buildSystemPrompt();
        String userPrompt = buildUserPrompt(charList, loreList, manuscriptText);
        
        String rawResponse = llmPort.generate(systemPrompt, userPrompt, 2500);

        // 4. Parser simple (pas de port spécifique, on retourne un Map)
        log.info("Script consistency checked");
        return Map.of("rawAnalysis", rawResponse, "status", "completed");
    }

    private String buildSystemPrompt() {
        return """
            Tu es un expert en analyse narrative.
            Tu évalues la cohérence d'un texte par rapport à la world-building et aux personnages.
            
            Réponds UNIQUEMENT en JSON valide.
            """;
    }

    private String buildUserPrompt(String charList, String loreList, String manuscriptText) {
        return """
            Analyse la cohérence de ce texte narratif.
            
            PERSONNAGES CONNUS :
            %s
            
            LORE/WORLD-BUILDING :
            %s
            
            TEXTE À ANALYSER :
            ---
            %s
            ---
            
            Identifie :
            1. Les mentions de personnages (nom mentionné + nombre de mentions)
            2. Les références au lore
            3. Les éventuelles incohérences (personnage mentionné mais pas défini, lore ignoré, etc.)
            
            Format JSON :
            {
              "character_mentions": [
                {"name": "Nom", "count": 5, "present": true}
              ],
              "lore_mentions": [
                {"title": "Concept", "mentioned": true, "references": 2}
              ],
              "issues": [
                {"type": "undefined_reference|missing_mention|inconsistency", "description": "..."}
              ]
            }
            
            Réponds UNIQUEMENT avec le JSON.
            """.formatted(charList, loreList, truncateText(manuscriptText, 5000));
    }

    private String truncateText(String text, int maxChars) {
        return text.length() > maxChars ? text.substring(0, maxChars) + "..." : text;
    }
}
