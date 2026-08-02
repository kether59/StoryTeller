package com.kether.storyteller.beforerefacto.usecase;

import com.kether.storyteller.infrastructure.web.rest.dto.Requests.TimelineEventUpdate;
import com.kether.storyteller.infrastructure.web.rest.dto.Responses.TimelineEventResponse;
import com.kether.storyteller.exception.ResourceNotFoundException;
import com.kether.storyteller.repository.TimelineEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional
public class UpdateTimelineEventUseCase {

    private final TimelineEventRepository timelineRepo;

    public TimelineEventResponse execute(Long eventId, TimelineEventUpdate request) {
        var event = timelineRepo.findById(eventId)
            .orElseThrow(() -> ResourceNotFoundException.of("Événement", eventId));

        if (request.title() != null) event.setTitle(request.title());
        if (request.date() != null) event.setDate(request.date());
        if (request.summary() != null) event.setSummary(request.summary());
        if (request.sortOrder() != null) event.setSortOrder(request.sortOrder());

        return TimelineEventResponse.from(timelineRepo.save(event));
    }
}
