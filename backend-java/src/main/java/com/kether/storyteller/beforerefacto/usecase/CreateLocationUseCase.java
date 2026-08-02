package com.kether.storyteller.beforerefacto.usecase;

import com.kether.storyteller.infrastructure.web.rest.dto.Requests.LocationCreate;
import com.kether.storyteller.infrastructure.web.rest.dto.Responses.LocationResponse;
import com.kether.storyteller.domain.entity.Story;
import com.kether.storyteller.domain.entity.StoryLocation;
import com.kether.storyteller.exception.ResourceNotFoundException;
import com.kether.storyteller.repository.LocationRepository;
import com.kether.storyteller.repository.StoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional
public class CreateLocationUseCase {

    private final LocationRepository locationRepo;
    private final StoryRepository storyRepo;

    public LocationResponse execute(LocationCreate request) {
        Story story = storyRepo.findById(request.storyId())
            .orElseThrow(() -> ResourceNotFoundException.of("Histoire", request.storyId()));

        var location = new StoryLocation();
        location.setStory(story);
        location.setName(request.name());
        location.setType(request.type());
        location.setSummary(request.summary());

        return LocationResponse.from(locationRepo.save(location));
    }
}
