package com.kether.storyteller.beforerefacto.usecase;

import com.kether.storyteller.infrastructure.web.rest.dto.Requests.TimelineEventCreate;
import com.kether.storyteller.infrastructure.web.rest.dto.Responses.TimelineEventResponse;
import com.kether.storyteller.domain.entity.Story;
import com.kether.storyteller.domain.entity.TimelineEvent;
import com.kether.storyteller.exception.ResourceNotFoundException;
import com.kether.storyteller.repository.TimelineEventRepository;
import com.kether.storyteller.repository.StoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional
public class CreateTimelineEventUseCase {

    private final TimelineEventRepository timelineRepo;
    private final StoryRepository storyRepo;

    public TimelineEventResponse execute(TimelineEventCreate request) {
        Story story = storyRepo.findById(request.storyId())
            .orElseThrow(() -> ResourceNotFoundException.of("Histoire", request.storyId()));

        var event = new TimelineEvent();
        event.setStory(story);
        event.setTitle(request.title());
        event.setDate(request.date());
        event.setSummary(request.summary());
        event.setSortOrder(request.sortOrder() != null ? request.sortOrder() : 0);

        return TimelineEventResponse.from(timelineRepo.save(event));
    }
}
