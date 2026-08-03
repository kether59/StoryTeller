package com.kether.storyteller.application.usecase.analysis;

import com.kether.storyteller.infrastructure.persistence.jpa.entity.LoreEntry;
import com.kether.storyteller.infrastructure.persistence.jpa.entity.Story;
import com.kether.storyteller.domain.port.out.llm.LLMGenerationPort;
import com.kether.storyteller.domain.port.out.persistence.LoreEntryRepositoryPort;
import com.kether.storyteller.domain.port.out.persistence.StoryRepositoryPort;
import com.kether.storyteller.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CheckLoreConsistencyUseCase {

    private final StoryRepositoryPort storyRepo;
    private final LoreEntryRepositoryPort loreRepo;
    private final LLMGenerationPort llmPort;

    public List<Map<String, Object>> execute(Long storyId, String manuscriptText) {
        Story story = storyRepo.findById(storyId)
                .orElseThrow(() -> ResourceNotFoundException.of("Story", storyId));

        List<LoreEntry> lore = loreRepo.findByStoryId(storyId);
        String prompt = """
            Vérifie la cohérence du lore dans ce texte.
            Histoire : %s
            Lore : %s
            Texte : %s
            Retourne un JSON avec "loreMentions": [{ "loreId": 1, "title": "...", "type": "...", "info": "..." }]
            """.formatted(story.getTitle(), formatLore(lore), manuscriptText);

        String raw = llmPort.generate("Tu es un gardien du lore vérifiant la cohérence.", prompt, 3000);
        return new ArrayList<>(List.of(Map.of("raw", raw, "status", "completed")));
    }

    private String formatLore(List<LoreEntry> lore) {
        return lore.stream()
                .map(l -> l.getTitle() + " (" + l.getCategory() + "): " + l.getContent())
                .reduce((a, b) -> a + "; " + b)
                .orElse("");
    }
}