package com.kether.storyteller.application.usecase.timeline;

import com.kether.storyteller.domain.port.out.persistence.TimelineEventRepositoryPort;
import com.kether.storyteller.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeleteTimelineEventUseCase {

    private final TimelineEventRepositoryPort timelineRepo;

    public void execute(Long id) {
        if (timelineRepo.findById(id).isEmpty()) {
            throw ResourceNotFoundException.of("TimelineEvent", id);
        }
        timelineRepo.deleteById(id);
    }
}