package com.kether.storyteller.infrastructure.persistence.repository;

import com.kether.storyteller.infrastructure.persistence.entity.StoryCharacter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CharacterRepository extends JpaRepository<StoryCharacter, Long> {
    List<StoryCharacter> findByStoryIdOrderByNameAsc(Long storyId);
    Optional<StoryCharacter> findByStoryIdAndName(Long storyId, String name);
    List<StoryCharacter> findByStoryId(Long storyId);
}
