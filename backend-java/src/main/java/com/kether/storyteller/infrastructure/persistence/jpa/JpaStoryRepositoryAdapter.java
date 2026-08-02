package com.kether.storyteller.infrastructure.persistence.jpa;

import com.kether.storyteller.domain.entity.Story;
import com.kether.storyteller.domain.port.out.persistence.StoryRepositoryPort;

import java.util.List;
import java.util.Optional;

public class JpaStoryRepositoryAdapter implements StoryRepositoryPort {
    private final SpringDataStoryRepository storyRepository;

    public JpaStoryRepositoryAdapter(SpringDataStoryRepository storyRepository) {
        this.storyRepository = storyRepository;
    }

    @Override
    public Optional<Story> findById(Long id) {
        return Optional.empty();
    }

    @Override
    public List<Story> findAll() {
        return List.of();
    }

    @Override
    public Story save(Story story) {
        return null;
    }

    @Override
    public void deleteById(Long id) {

    }


}
