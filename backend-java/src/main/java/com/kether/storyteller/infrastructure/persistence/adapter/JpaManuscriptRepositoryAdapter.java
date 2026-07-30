package com.kether.storyteller.infrastructure.persistence.adapter;

import com.kether.storyteller.domain.model.Manuscript;
import com.kether.storyteller.domain.port.out.ManuscriptRepositoryPort;
import com.kether.storyteller.infrastructure.persistence.repository.ManuscriptRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JpaManuscriptRepositoryAdapter implements ManuscriptRepositoryPort {

    // ← TON REPOSITORY EXISTANT, on y touche pas
    private final ManuscriptRepository existingRepo;

    @Override
    public Optional<Manuscript> findById(Long id) {
        return existingRepo.findById(id)
                .map(this::toDomain);
    }

    private Manuscript toDomain(com.kether.storyteller.infrastructure.persistence.entity.Manuscript jpa) {
        return new Manuscript(
                jpa.getId(),
                jpa.getStory().getId(),
                jpa.getTitle(),
                jpa.getChapter(),
                jpa.getText(),
                jpa.getStatus()
        );
    }
}