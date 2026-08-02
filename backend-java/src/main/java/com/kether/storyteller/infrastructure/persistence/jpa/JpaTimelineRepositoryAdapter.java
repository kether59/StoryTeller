package com.kether.storyteller.infrastructure.persistence.jpa;

import com.kether.storyteller.domain.model.ExtractedTimelineEvent;
import com.kether.storyteller.domain.model.TimelineConflict;
import com.kether.storyteller.domain.port.out.persistence.TimelineEventRepositoryPort;
import com.kether.storyteller.repository.TimelineEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class JpaTimelineRepositoryAdapter implements TimelineEventRepositoryPort {

    private final TimelineEventRepository existingRepo;

    @Override
    public List<ExtractedTimelineEvent> parse(String jsonResponse) {
        return List.of();
    }

    @Override
    public ExtractedTimelineEvent save(ExtractedTimelineEvent event) {
        // Pour l'instant, on retourne le domaine tel quel
        return event;
    }

    @Override
    public List<TimelineConflict> findConflictsByStoryId(Long storyId) {
        return List.of();
    }
}
