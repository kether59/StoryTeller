package com.kether.storyteller.beforerefacto.usecase;

import com.kether.storyteller.exception.ResourceNotFoundException;
import com.kether.storyteller.repository.TimelineEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional
public class DeleteTimelineEventUseCase {

    private final TimelineEventRepository timelineRepo;

    public void execute(Long eventId) {
        if (!timelineRepo.existsById(eventId))
            throw ResourceNotFoundException.of("Événement", eventId);
        timelineRepo.deleteById(eventId);
    }
}
