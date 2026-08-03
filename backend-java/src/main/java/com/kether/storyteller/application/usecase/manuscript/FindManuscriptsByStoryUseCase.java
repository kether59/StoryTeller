package com.kether.storyteller.application.usecase.manuscript;

import com.kether.storyteller.infrastructure.persistence.jpa.entity.Manuscript;
import com.kether.storyteller.domain.port.out.persistence.ManuscriptRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FindManuscriptsByStoryUseCase {

    private final ManuscriptRepositoryPort manuscriptRepo;

    public List<Manuscript> execute(Long storyId) {
        return manuscriptRepo.findByStoryId(storyId);
    }
}