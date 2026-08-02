package com.kether.storyteller.beforerefacto.usecase;

import com.kether.storyteller.domain.port.out.LLMPort;
import com.kether.storyteller.repository.LoreEntryRepository;
import com.kether.storyteller.repository.ManuscriptRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Use Case : Vérifier le lore pour les incohérences.
 * Analyse la cohérence du world-building et des règles établies.
 */
@Component
@RequiredArgsConstructor
public class CheckLoreConsistencyUseCase {

    private static final Logger log = LoggerFactory.getLogger(CheckLoreConsistencyUseCase.class);

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

        // 2. Récupérer le lore
        var loreEntries = loreRepo.findByStoryId(storyId);
        
        String loreDescriptions = loreEntries.stream()
                .map(e -> "- " + e.getTitle() + 
                        (e.getCategory() != null ? " (" + e.getCategory() + ")" : "") +
                        ": " + (e.getContent() != null ? truncateText(e.getContent(), 200) : ""))
                .reduce((a, b) -> a + "\n" + b)
                .orElse("(Aucun lore défini)");

        // 3. Appeler le LLM
        String systemPrompt = buildSystemPrompt();
        String userPrompt = buildUserPrompt(loreDescriptions, manuscriptText);
        
        String rawResponse = llmPort.generate(systemPrompt, userPrompt, 2500);

        log.info("Lore consistency checked");
        return Map.of("rawAnalysis", rawResponse, "status", "completed");
    }

    private String buildSystemPrompt() {
        return """
            Tu es un expert en world-building et en cohérence narrative.
            Tu identifies les violations du lore et les incohérences dans le world-building.
            
            Réponds UNIQUEMENT en JSON valide.
            """;
    }

    private String buildUserPrompt(String loreDescriptions, String manuscriptText) {
        return """
            Analyse la cohérence du lore et du world-building.
            
            LORE/RÈGLES ÉTABLIES :
            %s
            
            TEXTE À ANALYSER :
            ---
            %s
            ---
            
            Identifie :
            1. Les règles du lore respectées
            2. Les violations du lore ou du world-building
            3. Les incohérences narratives (magie utilisée contre les règles, etc.)
            4. Les éléments du lore non mentionnés mais relevant
            
            Format JSON :
            {
              "lore_respect": [
                {"rule": "...", "respected": true, "evidence": "..."}
              ],
              "violations": [
                {"rule": "...", "description": "...", "severity": "minor|major"}
              ],
              "suggestions": [
                "Suggestion pour améliorer la cohérence..."
              ]
            }
            
            Réponds UNIQUEMENT avec le JSON.
            """.formatted(loreDescriptions, truncateText(manuscriptText, 5000));
    }

    private String truncateText(String text, int maxChars) {
        return text.length() > maxChars ? text.substring(0, maxChars) + "..." : text;
    }
}
