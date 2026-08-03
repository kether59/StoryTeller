package com.kether.storyteller.application.usecase.location;

import com.kether.storyteller.infrastructure.persistence.jpa.entity.StoryLocation;
import com.kether.storyteller.domain.port.out.persistence.LocationRepositoryPort;
import com.kether.storyteller.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UpdateLocationUseCase {

    private final LocationRepositoryPort locationRepo;

    public StoryLocation execute(Long id, String name, String type, String summary) {
        StoryLocation location = locationRepo.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Location", id));

        if (name != null) location.setName(name);
        if (type != null) location.setType(type);
        if (summary != null) location.setSummary(summary);

        return locationRepo.save(location);
    }
}