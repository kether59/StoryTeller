package com.kether.storyteller.beforerefacto.usecase.character;

import com.kether.storyteller.domain.entity.StoryCharacter;
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