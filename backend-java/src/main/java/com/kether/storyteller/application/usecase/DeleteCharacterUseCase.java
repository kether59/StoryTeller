package com.kether.storyteller.application.usecase;

import com.kether.storyteller.exception.ResourceNotFoundException;
import com.kether.storyteller.repository.CharacterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional
public class DeleteCharacterUseCase {

    private final CharacterRepository characterRepo;

    public void execute(Long characterId) {
        if (!characterRepo.existsById(characterId))
            throw ResourceNotFoundException.of("Personnage", characterId);
        characterRepo.deleteById(characterId);
    }
}
