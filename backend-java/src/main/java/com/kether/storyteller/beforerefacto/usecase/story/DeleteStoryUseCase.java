package com.kether.storyteller.beforerefacto.usecase.story;

import com.kether.storyteller.domain.port.out.persistence.StoryRepositoryPort;
import com.kether.storyteller.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeleteStoryUseCase {

    private final StoryRepositoryPort storyRepo;

    public void execute(Long id) {
        if (storyRepo.findById(id).isEmpty()) {
            throw ResourceNotFoundException.of("Story", id);
        }
        storyRepo.deleteById(id);
    }
}