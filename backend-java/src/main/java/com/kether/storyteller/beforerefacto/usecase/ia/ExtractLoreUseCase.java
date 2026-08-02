package com.kether.storyteller.beforerefacto.usecase.ia;

import com.kether.storyteller.domain.entity.LoreEntry;
import com.kether.storyteller.domain.entity.Story;
import com.kether.storyteller.domain.model.ExtractedLore;
import com.kether.storyteller.domain.port.out.llm.LLMGenerationPort;
import com.kether.storyteller.domain.port.out.persistence.LoreEntryRepositoryPort;
import com.kether.storyteller.domain.port.out.persistence.StoryRepositoryPort;
import com.kether.storyteller.exception.ResourceNotFoundException;
import com.kether.storyteller.infrastructure.llm.parser.JacksonLoreExtractionParser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExtractLoreUseCase {

    private final StoryRepositoryPort storyRepo;
    private final LoreEntryRepositoryPort loreRepo;
    private final LLMGenerationPort llmPort;
    private final JacksonLoreExtractionParser parser;

    public List<LoreEntry> execute(Long storyId, String text) {
        Story story = storyRepo.findById(storyId)
                .orElseThrow(() -> ResourceNotFoundException.of("Story", storyId));

        String prompt = """
            Analyse ce texte et extrais les éléments de lore au format JSON.
            Histoire : %s
            Texte : %s
            Retourne { "lore": [{ "title": "...", "category": "...", "content": "..." }] }
            """.formatted(story.getTitle(), text);

        String raw = llmPort.generate("Tu es un extracteur de lore littéraire.", prompt, 2000);
        List<ExtractedLore> extracted = parser.parse(raw);

        return extracted.stream().map(e -> {
            LoreEntry entry = new LoreEntry();
            entry.setStory(story);
            entry.setTitle(e.title());
            entry.setCategory(e.category());
            entry.setContent(e.content());
            return loreRepo.save(entry);
        }).toList();
    }
}