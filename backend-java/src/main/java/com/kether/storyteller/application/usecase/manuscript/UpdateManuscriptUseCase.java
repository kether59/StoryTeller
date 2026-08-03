package com.kether.storyteller.application.usecase.manuscript;

import com.kether.storyteller.infrastructure.persistence.jpa.entity.Manuscript;
import com.kether.storyteller.domain.port.out.persistence.ManuscriptRepositoryPort;
import com.kether.storyteller.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UpdateManuscriptUseCase {

    private final ManuscriptRepositoryPort manuscriptRepo;

    public Manuscript execute(Long id, String title, Integer chapter, String text, String status) {
        Manuscript manuscript = manuscriptRepo.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Manuscript", id));

        if (title != null) manuscript.setTitle(title);
        if (chapter != null) manuscript.setChapter(chapter);
        if (text != null) manuscript.setText(text);
        if (status != null) manuscript.setStatus(status);

        return manuscriptRepo.save(manuscript);
    }
}