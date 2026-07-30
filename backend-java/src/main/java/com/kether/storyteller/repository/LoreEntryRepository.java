package com.kether.storyteller.repository;

import com.kether.storyteller.entity.LoreEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface LoreEntryRepository extends JpaRepository<LoreEntry, Long> {
    List<LoreEntry> findByStoryIdOrderByTitleAsc(Long storyId);
    Optional<LoreEntry> findByStoryIdAndTitle(Long storyId, String title);
    List<LoreEntry> findByStoryId(Long storyId);
}
