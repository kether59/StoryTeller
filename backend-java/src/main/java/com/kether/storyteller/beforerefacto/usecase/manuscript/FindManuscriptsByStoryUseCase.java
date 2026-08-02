package com.kether.storyteller.beforerefacto.usecase.manuscript;

import com.kether.storyteller.domain.entity.Manuscript;
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