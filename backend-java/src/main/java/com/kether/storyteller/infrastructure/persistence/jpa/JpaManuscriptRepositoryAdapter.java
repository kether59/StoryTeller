package com.kether.storyteller.infrastructure.persistence.jpa;

import com.kether.storyteller.domain.entity.Manuscript;
import com.kether.storyteller.domain.port.out.persistence.ManuscriptRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JpaManuscriptRepositoryAdapter implements ManuscriptRepositoryPort {

    private final SpringDataManuscriptRepository jpaRepo;

    @Override
    public Optional<Manuscript> findById(Long id) {
        return jpaRepo.findById(id);
    }

    @Override
    public List<Manuscript> findByStoryId(Long storyId) {
        return jpaRepo.findByStoryId(storyId);
    }

    @Override
    public List<Manuscript> findByStoryIdOrderByChapterAsc(Long storyId) {
        return jpaRepo.findByStoryIdOrderByChapterAsc(storyId);
    }

    @Override
    public List<Manuscript> findByStoryIdOrderByChapterDesc(Long storyId) {
        return jpaRepo.findByStoryIdOrderByChapterDesc(storyId);
    }

    @Override
    public Manuscript save(Manuscript manuscript) {
        return jpaRepo.save(manuscript);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepo.deleteById(id);
    }
}