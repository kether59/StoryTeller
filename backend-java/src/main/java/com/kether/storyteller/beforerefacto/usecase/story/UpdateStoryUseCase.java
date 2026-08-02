package com.kether.storyteller.beforerefacto.usecase.story;

import com.kether.storyteller.domain.entity.Story;
import com.kether.storyteller.domain.port.out.persistence.StoryRepositoryPort;
import com.kether.storyteller.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UpdateStoryUseCase {

    private final StoryRepositoryPort storyRepo;

    public Story execute(Long id, String title, String synopsis, String blurb) {
        Story story = storyRepo.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Story", id));

        if (title != null) story.setTitle(title);
        if (synopsis != null) story.setSynopsis(synopsis);
        if (blurb != null) story.setBlurb(blurb);

        return storyRepo.save(story);
    }
}