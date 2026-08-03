package com.kether.storyteller.infrastructure.persistence.jpa;

import com.kether.storyteller.infrastructure.persistence.jpa.entity.Manuscript;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpringDataManuscriptRepository extends JpaRepository<Manuscript, Long> {
    List<Manuscript> findByStoryIdOrderByChapterAsc(Long storyId);
    List<Manuscript> findByStoryIdOrderByChapterDesc(Long storyId);
    List<Manuscript> findByStoryId(Long storyId);
}