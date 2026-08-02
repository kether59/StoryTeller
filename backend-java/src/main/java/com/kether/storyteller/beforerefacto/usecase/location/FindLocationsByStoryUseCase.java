package com.kether.storyteller.beforerefacto.usecase.location;

import com.kether.storyteller.domain.entity.StoryLocation;
import com.kether.storyteller.domain.port.out.persistence.LocationRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FindLocationsByStoryUseCase {

    private final LocationRepositoryPort locationRepo;

    public List<StoryLocation> execute(Long storyId) {
        return locationRepo.findByStoryId(storyId);
    }
}