package com.kether.storyteller.infrastructure.persistence.jpa;

import com.kether.storyteller.infrastructure.persistence.jpa.entity.TimelineEvent;
import com.kether.storyteller.domain.port.out.persistence.TimelineEventRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JpaTimelineRepositoryAdapter implements TimelineEventRepositoryPort {

    private final SpringDataTimelineEventRepository jpaRepo;

    @Override
    public Optional<TimelineEvent> findById(Long id) {
        return jpaRepo.findById(id);
    }

    @Override
    public List<TimelineEvent> findByStoryId(Long storyId) {
        return jpaRepo.findByStoryId(storyId);
    }

    @Override
    public List<TimelineEvent> findByStoryIdOrderBySortOrderAsc(Long storyId) {
        return jpaRepo.findByStoryIdOrderBySortOrderAsc(storyId);
    }

    @Override
    public TimelineEvent save(TimelineEvent event) {
        return jpaRepo.save(event);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepo.deleteById(id);
    }
}