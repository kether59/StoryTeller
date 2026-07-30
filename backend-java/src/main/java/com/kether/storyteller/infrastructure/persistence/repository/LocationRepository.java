package com.kether.storyteller.infrastructure.persistence.repository;

import com.kether.storyteller.infrastructure.persistence.entity.StoryLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface LocationRepository extends JpaRepository<StoryLocation, Long> {
    List<StoryLocation> findByStoryIdOrderByNameAsc(Long storyId);
    Optional<StoryLocation> findByStoryIdAndName(Long storyId, String name);
}
