package com.kether.storyteller.application.usecase.story;

import com.kether.storyteller.infrastructure.persistence.jpa.entity.Story;
import com.kether.storyteller.domain.port.out.persistence.StoryRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CreateStoryUseCase {

    private final StoryRepositoryPort storyRepo;

    public Story execute(String title, String synopsis, String blurb) {
        Story story = new Story();
        story.setTitle(title);
        story.setSynopsis(synopsis);
        story.setBlurb(blurb);
        return storyRepo.save(story);
    }
}