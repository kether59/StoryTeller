package com.kether.storyteller.application.usecase.character;

import com.kether.storyteller.infrastructure.persistence.jpa.entity.Story;
import com.kether.storyteller.infrastructure.persistence.jpa.entity.StoryCharacter;
import com.kether.storyteller.domain.port.out.persistence.CharacterRepositoryPort;
import com.kether.storyteller.domain.port.out.persistence.StoryRepositoryPort;
import com.kether.storyteller.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CreateCharacterUseCase {

    private final StoryRepositoryPort storyRepo;
    private final CharacterRepositoryPort characterRepo;

    public StoryCharacter execute(Long storyId, String name, String role, String personality) {
        Story story = storyRepo.findById(storyId)
                .orElseThrow(() -> ResourceNotFoundException.of("Story", storyId));

        StoryCharacter character = new StoryCharacter();
        character.setStory(story);
        character.setName(name);
        character.setRole(role);
        character.setPersonality(personality);
        return characterRepo.save(character);
    }
}