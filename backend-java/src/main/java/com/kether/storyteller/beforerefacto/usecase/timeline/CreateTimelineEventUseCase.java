package com.kether.storyteller.beforerefacto.usecase.timeline;

import com.kether.storyteller.domain.entity.Story;
import com.kether.storyteller.domain.entity.TimelineEvent;
import com.kether.storyteller.domain.port.out.persistence.StoryRepositoryPort;
import com.kether.storyteller.domain.port.out.persistence.TimelineEventRepositoryPort;
import com.kether.storyteller.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CreateTimelineEventUseCase {

    private final StoryRepositoryPort storyRepo;
    private final TimelineEventRepositoryPort timelineRepo;

    public TimelineEvent execute(Long storyId, String title, String date, Integer sortOrder,
                                 String summary, Long locationId) {
        Story story = storyRepo.findById(storyId)
                .orElseThrow(() -> ResourceNotFoundException.of("Story", storyId));

        TimelineEvent event = new TimelineEvent();
        event.setStory(story);
        event.setTitle(title);
        event.setDate(date);
        event.setSortOrder(sortOrder);
        event.setSummary(summary);
        event.setLocationId(locationId);
        return timelineRepo.save(event);
    }
}