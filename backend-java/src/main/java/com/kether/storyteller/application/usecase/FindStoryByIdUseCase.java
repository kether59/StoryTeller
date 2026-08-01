package com.kether.storyteller.application.usecase;

import com.kether.storyteller.dto.response.Responses.StoryResponse;
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
