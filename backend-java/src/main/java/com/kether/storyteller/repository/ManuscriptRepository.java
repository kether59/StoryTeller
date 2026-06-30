package com.kether.storyteller.repository;

import com.kether.storyteller.entity.Manuscript;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ManuscriptRepository extends JpaRepository<Manuscript, Long> {
    List<Manuscript> findByStoryIdOrderByChapterAsc(Long storyId);
    List<Manuscript> findByStoryId(Long storyId);
}
