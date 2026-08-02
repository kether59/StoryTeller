package com.kether.storyteller.infrastructure.persistence.jpa;

import com.kether.storyteller.domain.entity.StoryLocation;
import com.kether.storyteller.domain.port.out.persistence.LocationRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JpaLocationRepositoryAdapter implements LocationRepositoryPort {

    private final SpringDataLocationRepository jpaRepo;

    @Override
    public Optional<StoryLocation> findById(Long id) {
        return jpaRepo.findById(id);
    }

    @Override
    public List<StoryLocation> findByStoryId(Long storyId) {
        return jpaRepo.findByStoryId(storyId);
    }

    @Override
    public List<StoryLocation> findByStoryIdOrderByNameAsc(Long storyId) {
        return jpaRepo.findByStoryIdOrderByNameAsc(storyId);
    }

    @Override
    public StoryLocation save(StoryLocation location) {
        return jpaRepo.save(location);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepo.deleteById(id);
    }
}