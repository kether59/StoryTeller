package com.kether.storyteller.application.usecase.character;

import com.kether.storyteller.infrastructure.persistence.jpa.entity.StoryCharacter;
import com.kether.storyteller.domain.port.out.persistence.CharacterRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FindCharactersByStoryUseCase {

    private final CharacterRepositoryPort characterRepo;

    public List<StoryCharacter> execute(Long storyId) {
        return characterRepo.findByStoryId(storyId);
    }
}