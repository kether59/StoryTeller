package com.kether.storyteller.application.usecase.analysis;

import com.kether.storyteller.infrastructure.persistence.jpa.entity.Story;
import com.kether.storyteller.domain.port.out.llm.LLMGenerationPort;
import com.kether.storyteller.domain.port.out.persistence.StoryRepositoryPort;
import com.kether.storyteller.exception.ResourceNotFoundException;
import com.kether.storyteller.infrastructure.web.rest.dto.Responses.ScriptConsistencyResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class CheckScriptConsistencyUseCase {

    private final StoryRepositoryPort storyRepo;
    private final LLMGenerationPort llmPort;
    private final JsonMapper mapper;

    public ScriptConsistencyResult execute(Long storyId, String manuscriptText) {
        Story story = storyRepo.findById(storyId)
                .orElseThrow(() -> ResourceNotFoundException.of("Story", storyId));

        String prompt = """
            Vérifie la cohérence générale de ce manuscrit avec l'histoire.
            Histoire : %s
            Synopsis : %s
            Texte : %s
            Retourne un JSON avec "mentions" et "loreMentions".
            """.formatted(story.getTitle(), story.getSynopsis(), manuscriptText);

        String raw = llmPort.generate("Tu es un éditeur vérifiant la cohérence narrative.", prompt, 3000);

        // Extraire le JSON du markdown
        String json = extractJsonFromMarkdown(raw);
        try {
            return mapper.readValue(json, ScriptConsistencyResult.class);
        } catch (Exception e) {
            // Fallback : retourner le brut pour debug
            return new ScriptConsistencyResult(null, null);
        }
    }


    private String extractJsonFromMarkdown(String raw) {
        if (raw == null) return "{}";
        // Nettoyer les backticks markdown
        return raw.replaceAll("(?s)^.*```json\\s*", "")
                .replaceAll("(?s)\\s*```.*$", "")
                .trim();
    }
}