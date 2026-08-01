package com.kether.storyteller.infrastructure.persistence.adapter;

import com.kether.storyteller.domain.model.ExtractedTimelineEvent;
import com.kether.storyteller.domain.port.out.TimelineRepositoryPort;
import com.kether.storyteller.repository.TimelineEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JpaTimelineRepositoryAdapter implements TimelineRepositoryPort {

    private final TimelineEventRepository existingRepo;

    @Override
    public ExtractedTimelineEvent save(ExtractedTimelineEvent event) {
        // Pour l'instant, on retourne le domaine tel quel
        return event;
    }
}
