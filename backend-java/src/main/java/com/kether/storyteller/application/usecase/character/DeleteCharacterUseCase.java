package com.kether.storyteller.application.usecase.character;

import com.kether.storyteller.domain.port.out.persistence.CharacterRepositoryPort;
import com.kether.storyteller.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeleteCharacterUseCase {

    private final CharacterRepositoryPort characterRepo;

    public void execute(Long id) {
        if (characterRepo.findById(id).isEmpty()) {
            throw ResourceNotFoundException.of("Character", id);
        }
        characterRepo.deleteById(id);
    }
}