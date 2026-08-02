package com.kether.storyteller.repository;

import com.kether.storyteller.domain.entity.TimelineEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TimelineEventRepository extends JpaRepository<TimelineEvent, Long> {
    List<TimelineEvent> findByStoryIdOrderBySortOrderAsc(Long storyId);

    @Query("""
        SELECT DISTINCT e FROM TimelineEvent e
        LEFT JOIN FETCH e.characters
        WHERE e.story.id = :storyId
        ORDER BY e.sortOrder ASC
    """)
    List<TimelineEvent> findByStoryIdWithCharacters(@Param("storyId") Long storyId);

    List<TimelineEvent> findByStoryId(Long storyId);
}
