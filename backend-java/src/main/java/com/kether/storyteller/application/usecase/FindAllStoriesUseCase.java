package com.kether.storyteller.application.usecase;

import com.kether.storyteller.dto.response.Responses.StoryResponse;
import com.kether.storyteller.repository.StoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Use Case : Récupérer toutes les histoires.
 */
@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FindAllStoriesUseCase {

    private final StoryRepository storyRepo;

    public List<StoryResponse> execute() {
        return storyRepo.findAll()
            .stream()
            .map(StoryResponse::from)
            .toList();
    }
}
