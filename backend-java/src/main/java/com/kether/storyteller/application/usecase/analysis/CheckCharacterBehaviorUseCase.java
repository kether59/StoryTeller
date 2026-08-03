package com.kether.storyteller.application.usecase.analysis;

import com.kether.storyteller.infrastructure.persistence.jpa.entity.Story;
import com.kether.storyteller.infrastructure.persistence.jpa.entity.StoryCharacter;
import com.kether.storyteller.domain.port.out.llm.LLMGenerationPort;
import com.kether.storyteller.domain.port.out.persistence.CharacterRepositoryPort;
import com.kether.storyteller.domain.port.out.persistence.StoryRepositoryPort;
import com.kether.storyteller.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CheckCharacterBehaviorUseCase {

    private final StoryRepositoryPort storyRepo;
    private final CharacterRepositoryPort characterRepo;
    private final LLMGenerationPort llmPort;

    public List<Map<String, Object>> execute(Long storyId, String manuscriptText) {
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
        // Parsing simplifié — tu peux créer un parser dédié si besoin
        return new ArrayList<>(List.of(Map.of("raw", raw, "status", "completed")));
    }

    private String formatChars(List<StoryCharacter> chars) {
        return chars.stream()
                .map(c -> c.getName() + " : " + c.getPersonality())
                .reduce((a, b) -> a + "; " + b)
                .orElse("");
    }
}