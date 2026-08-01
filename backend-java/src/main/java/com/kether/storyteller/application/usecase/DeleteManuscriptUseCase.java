package com.kether.storyteller.application.usecase;

import com.kether.storyteller.exception.ResourceNotFoundException;
import com.kether.storyteller.repository.ManuscriptRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional
public class DeleteManuscriptUseCase {

    private final ManuscriptRepository manuscriptRepo;

    public void execute(Long manuscriptId) {
        if (!manuscriptRepo.existsById(manuscriptId))
            throw ResourceNotFoundException.of("Manuscrit", manuscriptId);
        manuscriptRepo.deleteById(manuscriptId);
    }
}
