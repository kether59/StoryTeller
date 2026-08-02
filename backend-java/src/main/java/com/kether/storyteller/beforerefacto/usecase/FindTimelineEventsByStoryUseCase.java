package com.kether.storyteller.beforerefacto.usecase;

import com.kether.storyteller.infrastructure.web.rest.dto.Responses.TimelineEventResponse;
import com.kether.storyteller.repository.TimelineEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FindTimelineEventsByStoryUseCase {

    private final TimelineEventRepository timelineRepo;

    public List<TimelineEventResponse> execute(Long storyId) {
        return timelineRepo.findByStoryIdOrderBySortOrderAsc(storyId)
            .stream()
            .map(TimelineEventResponse::from)
            .toList();
    }
}
