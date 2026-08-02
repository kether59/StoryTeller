package com.kether.storyteller.beforerefacto.usecase.location;

import com.kether.storyteller.domain.port.out.persistence.LocationRepositoryPort;
import com.kether.storyteller.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeleteLocationUseCase {

    private final LocationRepositoryPort locationRepo;

    public void execute(Long id) {
        if (locationRepo.findById(id).isEmpty()) {
            throw ResourceNotFoundException.of("Location", id);
        }
        locationRepo.deleteById(id);
    }
}