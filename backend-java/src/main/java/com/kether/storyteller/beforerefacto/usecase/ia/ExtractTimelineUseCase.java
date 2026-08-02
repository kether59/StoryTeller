package com.kether.storyteller.beforerefacto.usecase.ia;

import com.kether.storyteller.domain.entity.Story;
import com.kether.storyteller.domain.model.ExtractedTimelineEvent;
import com.kether.storyteller.domain.port.out.llm.LLMGenerationPort;
import com.kether.storyteller.domain.port.out.persistence.StoryRepositoryPort;
import com.kether.storyteller.exception.ResourceNotFoundException;
import com.kether.storyteller.infrastructure.llm.parser.JacksonTimelineExtractionParser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExtractTimelineUseCase {

    private final StoryRepositoryPort storyRepo;
    private final LLMGenerationPort llmPort;
    private final JacksonTimelineExtractionParser parser;

    public List<ExtractedTimelineEvent> execute(Long storyId, String text) {
        Story story = storyRepo.findById(storyId)
                .orElseThrow(() -> ResourceNotFoundException.of("Story", storyId));

        String prompt = """
            Analyse ce texte et extrais les événements chronologiques au format JSON.
            Histoire : %s
            Texte : %s
            Retourne { "timeline": [{ "title": "...", "date": "...", "summary": "...", "sortOrder": 1, "characterNames": [], "locationName": "..." }] }
            """.formatted(story.getTitle(), text);

        String raw = llmPort.generate("Tu es un extracteur d'événements chronologiques.", prompt, 2000);
        return parser.parse(raw);
    }
}