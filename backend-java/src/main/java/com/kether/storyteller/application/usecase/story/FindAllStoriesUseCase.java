package com.kether.storyteller.application.usecase.story;

import com.kether.storyteller.infrastructure.persistence.jpa.entity.Story;
import com.kether.storyteller.domain.port.out.persistence.StoryRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FindAllStoriesUseCase {

    private final StoryRepositoryPort storyRepo;

    public List<Story> execute() {
        return storyRepo.findAll();
    }
}