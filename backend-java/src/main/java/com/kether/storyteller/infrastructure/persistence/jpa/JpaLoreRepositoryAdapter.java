package com.kether.storyteller.infrastructure.persistence.jpa;

import com.kether.storyteller.infrastructure.persistence.jpa.entity.LoreEntry;
import com.kether.storyteller.domain.port.out.persistence.LoreEntryRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JpaLoreRepositoryAdapter implements LoreEntryRepositoryPort {

    private final SpringDataLoreEntryRepository jpaRepo;

    @Override
    public Optional<LoreEntry> findById(Long id) {
        return jpaRepo.findById(id);
    }

    @Override
    public List<LoreEntry> findByStoryId(Long storyId) {
        return jpaRepo.findByStoryId(storyId);
    }

    @Override
    public List<LoreEntry> findByStoryIdOrderByTitleAsc(Long storyId) {
        return jpaRepo.findByStoryIdOrderByTitleAsc(storyId);
    }

    @Override
    public LoreEntry save(LoreEntry entry) {
        return jpaRepo.save(entry);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepo.deleteById(id);
    }
}