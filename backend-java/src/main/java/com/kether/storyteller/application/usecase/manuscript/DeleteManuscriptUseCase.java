package com.kether.storyteller.application.usecase.manuscript;

import com.kether.storyteller.domain.port.out.persistence.ManuscriptRepositoryPort;
import com.kether.storyteller.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeleteManuscriptUseCase {

    private final ManuscriptRepositoryPort manuscriptRepo;

    public void execute(Long id) {
        if (manuscriptRepo.findById(id).isEmpty()) {
            throw ResourceNotFoundException.of("Manuscript", id);
        }
        manuscriptRepo.deleteById(id);
    }
}