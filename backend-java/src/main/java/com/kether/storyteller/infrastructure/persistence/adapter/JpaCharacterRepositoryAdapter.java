package com.kether.storyteller.infrastructure.persistence.adapter;

import com.kether.storyteller.domain.port.out.CharacterRepositoryPort;
import com.kether.storyteller.infrastructure.persistence.repository.CharacterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JpaCharacterRepositoryAdapter implements CharacterRepositoryPort {

    private final CharacterRepository existingRepo;

    @Override
    public boolean existsByStoryIdAndName(Long storyId, String name) {
        return existingRepo.findByStoryIdAndName(storyId, name).isPresent();
    }
}