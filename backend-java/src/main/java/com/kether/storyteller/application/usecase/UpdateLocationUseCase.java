package com.kether.storyteller.application.usecase;

import com.kether.storyteller.dto.request.Requests.LocationUpdate;
import com.kether.storyteller.dto.response.Responses.LocationResponse;
import com.kether.storyteller.exception.ResourceNotFoundException;
import com.kether.storyteller.repository.LocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional
public class UpdateLocationUseCase {

    private final LocationRepository locationRepo;

    public LocationResponse execute(Long locationId, LocationUpdate request) {
        var location = locationRepo.findById(locationId)
            .orElseThrow(() -> ResourceNotFoundException.of("Lieu", locationId));

        if (request.name() != null) location.setName(request.name());
        if (request.type() != null) location.setType(request.type());
        if (request.summary() != null) location.setSummary(request.summary());

        return LocationResponse.from(locationRepo.save(location));
    }
}
