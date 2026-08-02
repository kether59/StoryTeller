package com.kether.storyteller.beforerefacto.usecase;

import com.kether.storyteller.exception.ResourceNotFoundException;
import com.kether.storyteller.repository.LocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional
public class DeleteLocationUseCase {

    private final LocationRepository locationRepo;

    public void execute(Long locationId) {
        if (!locationRepo.existsById(locationId))
            throw ResourceNotFoundException.of("Lieu", locationId);
        locationRepo.deleteById(locationId);
    }
}
