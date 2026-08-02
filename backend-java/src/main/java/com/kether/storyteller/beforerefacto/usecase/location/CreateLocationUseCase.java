package com.kether.storyteller.beforerefacto.usecase.location;

import com.kether.storyteller.domain.entity.Story;
import com.kether.storyteller.domain.entity.StoryLocation;
import com.kether.storyteller.domain.port.out.persistence.LocationRepositoryPort;
import com.kether.storyteller.domain.port.out.persistence.StoryRepositoryPort;
import com.kether.storyteller.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CreateLocationUseCase {

    private final StoryRepositoryPort storyRepo;
    private final LocationRepositoryPort locationRepo;

    public StoryLocation execute(Long storyId, String name, String type, String summary) {
        Story story = storyRepo.findById(storyId)
                .orElseThrow(() -> ResourceNotFoundException.of("Story", storyId));

        StoryLocation location = new StoryLocation();
        location.setStory(story);
        location.setName(name);
        location.setType(type);
        location.setSummary(summary);
        return locationRepo.save(location);
    }
}