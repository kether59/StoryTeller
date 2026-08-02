package com.kether.storyteller.infrastructure.persistence.jpa;

import com.kether.storyteller.domain.entity.Story;
import com.kether.storyteller.domain.port.out.persistence.StoryRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JpaStoryRepositoryAdapter implements StoryRepositoryPort {

    private final SpringDataStoryRepository jpaRepo;

    @Override
    public Optional<Story> findById(Long id) {
        return jpaRepo.findById(id);
    }

    @Override
    public List<Story> findAll() {
        return jpaRepo.findAll();
    }

    @Override
    public Story save(Story story) {
        return jpaRepo.save(story);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepo.deleteById(id);
    }
}