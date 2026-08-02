package com.kether.storyteller.beforerefacto.usecase.ia;

import com.kether.storyteller.domain.entity.Story;
import com.kether.storyteller.domain.entity.TimelineEvent;
import com.kether.storyteller.domain.model.TimelineConflict;
import com.kether.storyteller.domain.port.out.llm.LLMGenerationPort;
import com.kether.storyteller.domain.port.out.persistence.StoryRepositoryPort;
import com.kether.storyteller.domain.port.out.persistence.TimelineEventRepositoryPort;
import com.kether.storyteller.exception.ResourceNotFoundException;
import com.kether.storyteller.infrastructure.llm.parser.JacksonTimelineConflictParser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FindTimelineConflictsUseCase {

    private final StoryRepositoryPort storyRepo;
    private final TimelineEventRepositoryPort timelineRepo;
    private final LLMGenerationPort llmPort;
    private final JacksonTimelineConflictParser parser;

    public List<TimelineConflict> execute(Long storyId) {
        Story story = storyRepo.findById(storyId)
                .orElseThrow(() -> ResourceNotFoundException.of("Story", storyId));

        List<TimelineEvent> events = timelineRepo.findByStoryId(storyId);
        String prompt = buildPrompt(story, events);
        String raw = llmPort.generate("Tu es un analyste de cohérence chronologique.", prompt, 3000);
        return parser.parse(raw);
    }

    private String buildPrompt(Story story, List<TimelineEvent> events) {
        StringBuilder sb = new StringBuilder();
        sb.append("Analyse ces événements de l'histoire \"").append(story.getTitle()).append("\" et détecte les incohérences chronologiques:\n");
        events.forEach(e -> sb.append("- [").append(e.getSortOrder()).append("] ")
                .append(e.getDate()).append(" : ").append(e.getTitle())
                .append(" (").append(e.getSummary()).append(")\n"));
        sb.append("\nRetourne un JSON avec \"conflicts\": [{ \"eventId\": 1, \"characterId\": 2, \"reason\": \"...\" }]");
        return sb.toString();
    }
}