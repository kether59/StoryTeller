package com.kether.storyteller.application.usecase.timeline;

import com.kether.storyteller.infrastructure.persistence.jpa.entity.TimelineEvent;
import com.kether.storyteller.domain.port.out.persistence.TimelineEventRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FindTimelineEventsByStoryUseCase {

    private final TimelineEventRepositoryPort timelineRepo;

    public List<TimelineEvent> execute(Long storyId) {
        return timelineRepo.findByStoryId(storyId);
    }
}