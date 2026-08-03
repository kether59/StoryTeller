package com.kether.storyteller.infrastructure.persistence.jpa;

import com.kether.storyteller.infrastructure.persistence.jpa.entity.TimelineEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpringDataTimelineEventRepository extends JpaRepository<TimelineEvent, Long> {
    List<TimelineEvent> findByStoryIdOrderBySortOrderAsc(Long storyId);
    List<TimelineEvent> findByStoryId(Long storyId);
}