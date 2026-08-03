package com.kether.storyteller.application.usecase.extraction;

import com.kether.storyteller.infrastructure.persistence.jpa.entity.Story;
import com.kether.storyteller.domain.model.ExtractedLore;
import com.kether.storyteller.domain.port.out.llm.LLMGenerationPort;
import com.kether.storyteller.domain.port.out.llm.LoreExtractionParserPort;
import com.kether.storyteller.domain.port.out.persistence.StoryRepositoryPort;
import com.kether.storyteller.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExtractLoreUseCase {

    private final StoryRepositoryPort storyRepo;
    private final LLMGenerationPort llmPort;
    private final LoreExtractionParserPort parser;

    public List<ExtractedLore> execute(Long storyId, String text) {
        Story story = storyRepo.findById(storyId).orElseThrow(() -> ResourceNotFoundException.of("Story", storyId));

        String prompt = """
                Analyse ce texte et extrais les éléments de lore au format JSON.
                Histoire : %s
                Texte : %s
                Retourne { "lore": [{ "title": "...", "category": "...", "content": "..." }] }
                """.formatted(story.getTitle(), text);

        String raw = llmPort.generate("Tu es un extracteur de lore littéraire.", prompt, 2000);
        return parser.parse(raw);
    }
}