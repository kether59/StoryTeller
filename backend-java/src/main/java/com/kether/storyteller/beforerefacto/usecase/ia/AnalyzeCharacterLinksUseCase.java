package com.kether.storyteller.beforerefacto.usecase.ia;

import com.kether.storyteller.domain.entity.Story;
import com.kether.storyteller.domain.entity.StoryCharacter;
import com.kether.storyteller.domain.model.CharacterRelationship;
import com.kether.storyteller.domain.port.out.llm.LLMGenerationPort;
import com.kether.storyteller.domain.port.out.persistence.CharacterRepositoryPort;
import com.kether.storyteller.domain.port.out.persistence.StoryRepositoryPort;
import com.kether.storyteller.exception.ResourceNotFoundException;
import com.kether.storyteller.infrastructure.llm.parser.JacksonRelationshipParser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AnalyzeCharacterLinksUseCase {

    private final StoryRepositoryPort storyRepo;
    private final CharacterRepositoryPort characterRepo;
    private final LLMGenerationPort llmPort;
    private final JacksonRelationshipParser parser;

    public List<CharacterRelationship> execute(Long storyId) {
        Story story = storyRepo.findById(storyId)
                .orElseThrow(() -> ResourceNotFoundException.of("Story", storyId));

        List<StoryCharacter> characters = characterRepo.findByStoryId(storyId);
        String prompt = buildPrompt(story, characters);
        String raw = llmPort.generate("Tu es un analyste de relations entre personnages.", prompt, 3000);
        return parser.parse(raw);
    }

    private String buildPrompt(Story story, List<StoryCharacter> characters) {
        StringBuilder sb = new StringBuilder();
        sb.append("Analyse les relations entre ces personnages de l'histoire \"").append(story.getTitle()).append("\":\n");
        characters.forEach(c -> sb.append("- ").append(c.getName()).append(" (").append(c.getRole()).append(")\n"));
        sb.append("\nRetourne un JSON avec une clé \"relationships\" contenant des objets : { \"character1\": \"...\", \"character2\": \"...\", \"type\": \"...\", \"description\": \"...\", \"confidence\": 0.8, \"evidence\": \"...\" }");
        return sb.toString();
    }
}