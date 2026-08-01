package com.kether.storyteller.application.usecase;

import com.kether.storyteller.dto.request.Requests.ManuscriptUpdate;
import com.kether.storyteller.dto.response.Responses.ManuscriptResponse;
import com.kether.storyteller.exception.ResourceNotFoundException;
import com.kether.storyteller.repository.ManuscriptRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional
public class UpdateManuscriptUseCase {

    private final ManuscriptRepository manuscriptRepo;

    public ManuscriptResponse execute(Long manuscriptId, ManuscriptUpdate request) {
        var manuscript = manuscriptRepo.findById(manuscriptId)
            .orElseThrow(() -> ResourceNotFoundException.of("Manuscrit", manuscriptId));

        if (request.title() != null) manuscript.setTitle(request.title());
        if (request.chapter() != null) manuscript.setChapter(request.chapter());
        if (request.text() != null) manuscript.setText(request.text());
        if (request.status() != null) manuscript.setStatus(request.status());

        return ManuscriptResponse.from(manuscriptRepo.save(manuscript));
    }
}
