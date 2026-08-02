package com.kether.storyteller.beforerefacto.usecase;

import com.kether.storyteller.domain.model.CharacterRelationship;
import com.kether.storyteller.domain.port.out.LLMPort;
import com.kether.storyteller.domain.port.out.persistence.RelationshipParserPort;
import com.kether.storyteller.repository.CharacterRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Use Case : Analyser les liens entre personnages via IA.
 * Orchestre LLM + parser de relations.
 */
@Component
@RequiredArgsConstructor
public class AnalyzeCharacterLinksUseCase {

    private static final Logger log = LoggerFactory.getLogger(AnalyzeCharacterLinksUseCase.class);

    private final CharacterRepository characterRepo;
    private final LLMPort llmPort;
    private final RelationshipParserPort relationshipParser;

    public List<CharacterRelationship> execute(Long storyId, String manuscriptText) {
        // 1. Récupérer les personnages
        var characters = characterRepo.findByStoryIdOrderByNameAsc(storyId);
        
        if (characters.isEmpty()) {
            return List.of();
        }

        // 2. Construire la description des personnages
        String charDescriptions = characters.stream()
                .map(c -> "- " + c.getName() + 
                        (c.getSurname() != null ? " " + c.getSurname() : "") +
                        (c.getRole() != null ? " (" + c.getRole() + ")" : ""))
                .reduce((a, b) -> a + "\n" + b)
                .orElse("");

        // 3. Appeler le LLM
        String systemPrompt = buildSystemPrompt();
        String userPrompt = buildUserPrompt(charDescriptions, manuscriptText);
        
        String rawResponse = llmPort.generate(systemPrompt, userPrompt, 2500);

        // 4. Parser et retourner
        try {
            List<CharacterRelationship> relationships = relationshipParser.parse(rawResponse);
            log.info("Character links analyzed — count={}", relationships.size());
            return relationships;
        } catch (Exception e) {
            log.error("Erreur lors de l'analyse des liens : {}", e.getMessage());
            return List.of();
        }
    }

    private String buildSystemPrompt() {
        return """
            Tu es un expert en analyse narrative et en relations sociales.
            Tu identifies les relations entre personnages avec précision et nuance.
            
            Réponds UNIQUEMENT en JSON valide, sans texte supplémentaire.
            """;
    }

    private String buildUserPrompt(String charDescriptions, String manuscriptText) {
        String excerpt = manuscriptText == null || manuscriptText.isEmpty() 
            ? "" 
            : "\nTEXTE DU MANUSCRIT (si disponible):\n---\n" + 
              truncateText(manuscriptText, 4000) + "\n---";

        String textAnalysis = manuscriptText == null || manuscriptText.isEmpty() 
            ? "" 
            : "\nIdentifie aussi les relations explicites du texte (alliances, conflits, etc.)";

        return """
            Analyse les relations potentielles entre ces personnages.
            
            PERSONNAGES :
            %s
            %s
            
            Format JSON requis :
            {
              "relationships": [
                {
                  "character_1": "Nom Complet",
                  "character_2": "Nom Complet",
                  "type": "ally|rival|family|romantic|mentor|enemy|neutral",
                  "description": "Explication brève de la relation",
                  "confidence": 0.8,
                  "evidence": "citation du texte ou explication"
                }
              ]
            }
            
            Repère les relations basées sur :
            - Noms de famille (relations familiales)
            - Écarts d'âge (pairs, mentor)
            - Rôles complémentaires (maître/apprenti)
            %s
            
            Réponds UNIQUEMENT avec le JSON.
            """.formatted(charDescriptions, excerpt, textAnalysis);
    }

    private String truncateText(String text, int maxChars) {
        return text.length() > maxChars ? text.substring(0, maxChars) + "..." : text;
    }
}
