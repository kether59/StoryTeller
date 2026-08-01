package com.kether.storyteller.application.usecase;

import com.kether.storyteller.dto.response.Responses.ManuscriptResponse;
import com.kether.storyteller.repository.ManuscriptRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FindManuscriptsByStoryUseCase {

    private final ManuscriptRepository manuscriptRepo;

    public List<ManuscriptResponse> execute(Long storyId) {
        return manuscriptRepo.findByStoryIdOrderByChapterAsc(storyId)
            .stream()
            .map(ManuscriptResponse::from)
            .toList();
    }
}
