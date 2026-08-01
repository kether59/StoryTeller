package com.kether.storyteller.application.usecase;

import com.kether.storyteller.domain.model.ExtractedLocation;
import com.kether.storyteller.domain.model.Manuscript;
import com.kether.storyteller.domain.port.out.LocationExtractionParserPort;
import com.kether.storyteller.domain.port.out.LocationRepositoryPort;
import com.kether.storyteller.domain.port.out.LLMPort;
import com.kether.storyteller.domain.port.out.ManuscriptRepositoryPort;
import com.kether.storyteller.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ExtractLocationsUseCase {

    private final ManuscriptRepositoryPort manuscriptRepo;
    private final LLMPort llmPort;
    private final LocationRepositoryPort locationRepo;
    private final LocationExtractionParserPort parser;

    public List<ExtractedLocation> execute(Long manuscriptId) {
        // 1. Récupérer le manuscrit
        Manuscript manuscript = manuscriptRepo.findById(manuscriptId)
                .orElseThrow(() -> ResourceNotFoundException.of("Manuscrit", manuscriptId));

        if (manuscript.isTooShortForAnalysis()) {
            throw new IllegalArgumentException("Le manuscrit est trop court pour être analysé");
        }

        // 2. Appeler le LLM
        String rawResponse = llmPort.generate(
                buildSystemPrompt(),
                buildUserPrompt(manuscript.text()),
                4000
        );

        // 3. Parser la réponse
        List<ExtractedLocation> extracted = parser.parse(rawResponse);

        // 4. Filtrer les doublons (règle métier)
        return extracted.stream()
                .filter(l -> !locationRepo.existsByStoryIdAndName(manuscript.storyId(), l.name()))
                .toList();
    }

    private String buildSystemPrompt() {
        return """
            Tu es un assistant expert en analyse narrative.
            Tu extrais des informations structurées depuis des textes littéraires.
            Réponds UNIQUEMENT en JSON valide, sans texte supplémentaire.
            Tout le contenu doit être en FRANÇAIS.
            """;
    }

    private String buildUserPrompt(String text) {
        String excerpt = text.length() > 8000 ? text.substring(0, 8000) : text;
        return """
            Analyse ce texte et extrais les lieux.
            
            TEXTE :
            ---
            %s
            ---
            
            Format JSON :
            {
              "locations": [
                {
                  "name": "...", "type": "...", "summary": "...",
                  "confidence": 0.9
                }
              ]
            }
            """.formatted(excerpt);
    }
}
