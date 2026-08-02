package com.kether.storyteller.beforerefacto.usecase;

import com.kether.storyteller.infrastructure.web.rest.dto.Requests.StoryCreate;
import com.kether.storyteller.infrastructure.web.rest.dto.Responses.StoryResponse;
import com.kether.storyteller.domain.entity.Story;
import com.kether.storyteller.repository.StoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use Case : Créer une nouvelle histoire.
 */
@Component
@RequiredArgsConstructor
@Transactional
public class CreateStoryUseCase {

    private final StoryRepository storyRepo;

    public StoryResponse execute(StoryCreate request) {
        var story = new Story(
            request.title(),
            request.synopsis(),
            request.blurb()
        );
        return StoryResponse.from(storyRepo.save(story));
    }
}

