package com.kether.storyteller.application.usecase.analysis;

import com.kether.storyteller.infrastructure.persistence.jpa.entity.Story;
import com.kether.storyteller.infrastructure.persistence.jpa.entity.StoryCharacter;
import com.kether.storyteller.domain.port.out.llm.LLMGenerationPort;
import com.kether.storyteller.domain.port.out.persistence.CharacterRepositoryPort;
import com.kether.storyteller.domain.port.out.persistence.StoryRepositoryPort;
import com.kether.storyteller.exception.ResourceNotFoundException;
import com.kether.storyteller.infrastructure.web.rest.dto.Responses;
import com.kether.storyteller.infrastructure.web.rest.dto.Responses.BehaviorResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CheckCharacterBehaviorUseCase {

    private final StoryRepositoryPort storyRepo;
    private final CharacterRepositoryPort characterRepo;
    private final LLMGenerationPort llmPort;
    private final JsonMapper mapper;

    public BehaviorResult execute(Long storyId, String manuscriptText) {
        Story story = storyRepo.findById(storyId)
                .orElseThrow(() -> ResourceNotFoundException.of("Story", storyId));

        List<StoryCharacter> characters = characterRepo.findByStoryId(storyId);
        String prompt = """
            Vérifie la cohérence comportementale des personnages dans ce texte.
            Histoire : %s
            Personnages : %s
            Texte : %s
            Retourne un JSON avec "behaviorIssues": [{ "characterId": 1, "charName": "...", "actionFound": "...", "conflictingTrait": "...", "context": "...", "reason": "..." }]
            """.formatted(story.getTitle(), formatChars(characters), manuscriptText);

        String raw = llmPort.generate("Tu es un éditeur vérifiant la cohérence des personnages.", prompt, 3000);
        // Extraire le JSON du markdown
        String json = extractJsonFromMarkdown(raw);
        try {
            return mapper.readValue(json, Responses.BehaviorResult.class);
        } catch (Exception e) {
            // Fallback : retourner le brut pour debug
            return new Responses.BehaviorResult(null);
        }
    }

    private String formatChars(List<StoryCharacter> chars) {
        return chars.stream()
                .map(c -> c.getName() + " : " + c.getPersonality())
                .reduce((a, b) -> a + "; " + b)
                .orElse("");
    }

    private String extractJsonFromMarkdown(String raw) {
        if (raw == null) return "{}";
        // Nettoyer les backticks markdown
        return raw.replaceAll("(?s)^.*```json\\s*", "")
                .replaceAll("(?s)\\s*```.*$", "")
                .trim();
    }
}