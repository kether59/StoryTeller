package com.kether.storyteller.infrastructure.persistence.jpa;

import com.kether.storyteller.infrastructure.persistence.jpa.entity.StoryCharacter;
import com.kether.storyteller.domain.port.out.persistence.CharacterRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JpaCharacterRepositoryAdapter implements CharacterRepositoryPort {

    private final SpringDataCharacterRepository jpaRepo;

    @Override
    public Optional<StoryCharacter> findById(Long id) {
        return jpaRepo.findById(id);
    }

    @Override
    public List<StoryCharacter> findByStoryId(Long storyId) {
        return jpaRepo.findByStoryId(storyId);
    }

    @Override
    public List<StoryCharacter> findByStoryIdOrderByNameAsc(Long storyId) {
        return jpaRepo.findByStoryIdOrderByNameAsc(storyId);
    }

    @Override
    public StoryCharacter save(StoryCharacter character) {
        return jpaRepo.save(character);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepo.deleteById(id);
    }
}