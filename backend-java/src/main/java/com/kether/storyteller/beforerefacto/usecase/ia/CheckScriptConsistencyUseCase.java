package com.kether.storyteller.beforerefacto.usecase.ia;

import com.kether.storyteller.domain.entity.Story;
import com.kether.storyteller.domain.port.out.llm.LLMGenerationPort;
import com.kether.storyteller.domain.port.out.persistence.StoryRepositoryPort;
import com.kether.storyteller.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class CheckScriptConsistencyUseCase {

    private final StoryRepositoryPort storyRepo;
    private final LLMGenerationPort llmPort;

    public Map<String, Object> execute(Long storyId, String manuscriptText) {
        Story story = storyRepo.findById(storyId)
                .orElseThrow(() -> ResourceNotFoundException.of("Story", storyId));

        String prompt = """
            Vérifie la cohérence générale de ce manuscrit avec l'histoire.
            Histoire : %s
            Synopsis : %s
            Texte : %s
            Retourne un JSON avec "mentions" et "loreMentions".
            """.formatted(story.getTitle(), story.getSynopsis(), manuscriptText);

        llmPort.generate("Tu es un éditeur vérifiant la cohérence narrative.", prompt, 3000);
        return Map.of();
    }
}