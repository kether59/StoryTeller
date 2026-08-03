package com.kether.storyteller.application.usecase.timeline;

import com.kether.storyteller.infrastructure.persistence.jpa.entity.TimelineEvent;
import com.kether.storyteller.domain.port.out.persistence.TimelineEventRepositoryPort;
import com.kether.storyteller.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UpdateTimelineEventUseCase {

    private final TimelineEventRepositoryPort timelineRepo;

    public TimelineEvent execute(Long id, String title, String date, Integer sortOrder,
                                 String summary, Long locationId) {
        TimelineEvent event = timelineRepo.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("TimelineEvent", id));

        if (title != null) event.setTitle(title);
        if (date != null) event.setDate(date);
        if (sortOrder != null) event.setSortOrder(sortOrder);
        if (summary != null) event.setSummary(summary);
        if (locationId != null) event.setLocationId(locationId);

        return timelineRepo.save(event);
    }
}