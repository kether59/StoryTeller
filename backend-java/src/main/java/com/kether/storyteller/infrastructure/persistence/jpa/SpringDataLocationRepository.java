package com.kether.storyteller.infrastructure.persistence.jpa;

import com.kether.storyteller.infrastructure.persistence.jpa.entity.StoryLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SpringDataLocationRepository extends JpaRepository<StoryLocation, Long> {
    List<StoryLocation> findByStoryIdOrderByNameAsc(Long storyId);
    Optional<StoryLocation> findByStoryIdAndName(Long storyId, String name);
    List<StoryLocation> findByStoryId(Long storyId);
}