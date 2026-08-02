package com.kether.storyteller.infrastructure.persistence.jpa;

import com.kether.storyteller.domain.model.Manuscript;
import com.kether.storyteller.domain.port.out.persistence.ManuscriptRepositoryPort;
import com.kether.storyteller.repository.ManuscriptRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JpaManuscriptRepositoryAdapter implements ManuscriptRepositoryPort {

     private final ManuscriptRepository existingRepo;

    @Override
    public Optional<Manuscript> findById(Long id) {
        return existingRepo.findById(id)
                .map(this::toDomain);
    }

    private Manuscript toDomain(com.kether.storyteller.entity.Manuscript jpa) {
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