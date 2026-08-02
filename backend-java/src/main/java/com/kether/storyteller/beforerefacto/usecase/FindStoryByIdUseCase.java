package com.kether.storyteller.beforerefacto.usecase;

import com.kether.storyteller.infrastructure.web.rest.dto.Responses.StoryResponse;
import com.kether.storyteller.exception.ResourceNotFoundException;
import com.kether.storyteller.repository.StoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use Case : Récupérer une histoire par ID.
 */
@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FindStoryByIdUseCase {

    private final StoryRepository storyRepo;

    public StoryResponse execute(Long storyId) {
        return StoryResponse.from(
            storyRepo.findById(storyId)
                .orElseThrow(() -> ResourceNotFoundException.of("Histoire", storyId))
        );
    }
}
