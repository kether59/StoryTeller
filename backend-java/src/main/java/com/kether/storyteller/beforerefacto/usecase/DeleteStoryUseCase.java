package com.kether.storyteller.beforerefacto.usecase;

import com.kether.storyteller.exception.ResourceNotFoundException;
import com.kether.storyteller.repository.StoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use Case : Supprimer une histoire.
 */
@Component
@RequiredArgsConstructor
@Transactional
public class DeleteStoryUseCase {

    private final StoryRepository storyRepo;

    public void execute(Long storyId) {
        if (!storyRepo.existsById(storyId))
            throw ResourceNotFoundException.of("Histoire", storyId);
        storyRepo.deleteById(storyId);
    }
}
