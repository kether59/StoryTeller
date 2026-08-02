package com.kether.storyteller.infrastructure.persistence.jpa;

import com.kether.storyteller.domain.entity.LoreEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SpringDataLoreEntryRepository extends JpaRepository<LoreEntry, Long> {
    List<LoreEntry> findByStoryIdOrderByTitleAsc(Long storyId);
    Optional<LoreEntry> findByStoryIdAndTitle(Long storyId, String title);
    List<LoreEntry> findByStoryId(Long storyId);
}