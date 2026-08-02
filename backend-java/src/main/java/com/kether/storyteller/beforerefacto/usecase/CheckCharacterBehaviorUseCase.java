package com.kether.storyteller.beforerefacto.usecase;

import com.kether.storyteller.domain.port.out.LLMPort;
import com.kether.storyteller.repository.CharacterRepository;
import com.kether.storyteller.repository.ManuscriptRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Use Case : Vérifier la cohérence du comportement des personnages.
 * Utilise l'IA pour détecter les comportements incohérents ou non-vraisemblables.
 */
@Component
@RequiredArgsConstructor
public class CheckCharacterBehaviorUseCase {

    private static final Logger log = LoggerFactory.getLogger(CheckCharacterBehaviorUseCase.class);

    private final CharacterRepository characterRepo;
    private final ManuscriptRepository manuscriptRepo;
    private final LLMPort llmPort;

    public Map<String, Object> execute(Long storyId, Long manuscriptId) {
        // 1. Récupérer le manuscrit
        String manuscriptText = manuscriptId != null
            ? manuscriptRepo.findById(manuscriptId)
                .map(m -> m.getText())
                .orElse("")
            : "";

        // 2. Récupérer les personnages
        var characters = characterRepo.findByStoryIdOrderByNameAsc(storyId);
        
        if (characters.isEmpty()) {
            return Map.of("issues", List.of(), "status", "no_characters");
        }

        // 3. Construire descriptions
        String charDescriptions = characters.stream()
                .map(c -> "- " + c.getName() + 
                        (c.getRole() != null ? " (" + c.getRole() + ")" : "") +
                        (c.getPersonality() != null ? " — " + c.getPersonality() : ""))
                .reduce((a, b) -> a + "\n" + b)
                .orElse("");

        // 4. Appeler le LLM
        String systemPrompt = buildSystemPrompt();
        String userPrompt = buildUserPrompt(charDescriptions, manuscriptText);
        
        String rawResponse = llmPort.generate(systemPrompt, userPrompt, 2500);

        log.info("Character behavior checked");
        return Map.of("rawAnalysis", rawResponse, "status", "completed");
    }

    private String buildSystemPrompt() {
        return """
            Tu es un expert en psychologie narrative et en développement de personnages.
            Tu identifies les comportements incohérents ou non-vraisemblables dans un texte.
            
            Réponds UNIQUEMENT en JSON valide.
            """;
    }

    private String buildUserPrompt(String charDescriptions, String manuscriptText) {
        return """
            Analyse la cohérence comportementale des personnages.
            
            PROFILS DES PERSONNAGES :
            %s
            
            TEXTE À ANALYSER :
            ---
            %s
            ---
            
            Identifie :
            1. Les actions/paroles cohérentes avec leur profil
            2. Les comportements qui semblent hors caractère
            3. Les évolutions non justifiées
            
            Format JSON :
            {
              "character_analyses": [
                {
                  "character": "Nom",
                  "consistent_actions": ["action 1", "action 2"],
                  "inconsistencies": [
                    {"action": "...", "reason": "...", "severity": "minor|major"}
                  ]
                }
              ]
            }
            
            Réponds UNIQUEMENT avec le JSON.
            """.formatted(charDescriptions, truncateText(manuscriptText, 5000));
    }

    private String truncateText(String text, int maxChars) {
        return text.length() > maxChars ? text.substring(0, maxChars) + "..." : text;
    }
}
