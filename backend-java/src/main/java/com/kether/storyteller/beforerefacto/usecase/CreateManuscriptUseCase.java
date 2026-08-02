package com.kether.storyteller.beforerefacto.usecase;

import com.kether.storyteller.infrastructure.web.rest.dto.Requests.ManuscriptCreate;
import com.kether.storyteller.infrastructure.web.rest.dto.Responses.ManuscriptResponse;
import com.kether.storyteller.domain.entity.Story;
import com.kether.storyteller.entity.Manuscript;
import com.kether.storyteller.exception.ResourceNotFoundException;
import com.kether.storyteller.repository.ManuscriptRepository;
import com.kether.storyteller.repository.StoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional
public class CreateManuscriptUseCase {

    private final ManuscriptRepository manuscriptRepo;
    private final StoryRepository storyRepo;

    public ManuscriptResponse execute(ManuscriptCreate request) {
        Story story = storyRepo.findById(request.storyId())
            .orElseThrow(() -> ResourceNotFoundException.of("Histoire", request.storyId()));

        var manuscript = new Manuscript();
        manuscript.setStory(story);
        manuscript.setTitle(request.title());
        manuscript.setChapter(request.chapter());
        manuscript.setText(request.text());
        manuscript.setStatus(request.status() != null ? request.status() : "draft");

        return ManuscriptResponse.from(manuscriptRepo.save(manuscript));
    }
}
