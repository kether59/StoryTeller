package com.kether.storyteller.application.usecase.manuscript;

import com.kether.storyteller.infrastructure.persistence.jpa.entity.Manuscript;
import com.kether.storyteller.infrastructure.persistence.jpa.entity.Story;
import com.kether.storyteller.domain.port.out.persistence.ManuscriptRepositoryPort;
import com.kether.storyteller.domain.port.out.persistence.StoryRepositoryPort;
import com.kether.storyteller.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CreateManuscriptUseCase {

    private final StoryRepositoryPort storyRepo;
    private final ManuscriptRepositoryPort manuscriptRepo;

    public Manuscript execute(Long storyId, String title, Integer chapter, String text, String status) {
        Story story = storyRepo.findById(storyId)
                .orElseThrow(() -> ResourceNotFoundException.of("Story", storyId));

        Manuscript manuscript = new Manuscript();
        manuscript.setStory(story);
        manuscript.setTitle(title);
        manuscript.setChapter(chapter);
        manuscript.setText(text);
        manuscript.setStatus(status != null ? status : "draft");
        return manuscriptRepo.save(manuscript);
    }
}