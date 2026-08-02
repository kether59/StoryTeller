package com.kether.storyteller.beforerefacto.usecase;

import com.kether.storyteller.infrastructure.web.rest.dto.Requests.StoryUpdate;
import com.kether.storyteller.infrastructure.web.rest.dto.Responses.StoryResponse;
import com.kether.storyteller.exception.ResourceNotFoundException;
import com.kether.storyteller.repository.StoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use Case : Mettre à jour une histoire.
 */
@Component
@RequiredArgsConstructor
@Transactional
public class UpdateStoryUseCase {

    private final StoryRepository storyRepo;

    public StoryResponse execute(Long storyId, StoryUpdate request) {
        var story = storyRepo.findById(storyId)
            .orElseThrow(() -> ResourceNotFoundException.of("Histoire", storyId));

        if (request.title() != null)
            story.setTitle(request.title());
        if (request.synopsis() != null)
            story.setSynopsis(request.synopsis());
        if (request.blurb() != null)
            story.setBlurb(request.blurb());

        return StoryResponse.from(storyRepo.save(story));
    }
}
