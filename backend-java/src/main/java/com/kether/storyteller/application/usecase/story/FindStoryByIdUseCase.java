package com.kether.storyteller.application.usecase.story;

import com.kether.storyteller.infrastructure.persistence.jpa.entity.Story;
import com.kether.storyteller.domain.port.out.persistence.StoryRepositoryPort;
import com.kether.storyteller.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FindStoryByIdUseCase {

    private final StoryRepositoryPort storyRepo;

    public Story execute(Long id) {
        return storyRepo.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Story", id));
    }
}