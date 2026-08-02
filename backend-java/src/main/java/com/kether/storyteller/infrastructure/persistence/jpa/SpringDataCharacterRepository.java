package com.kether.storyteller.infrastructure.persistence.jpa;

import com.kether.storyteller.domain.entity.StoryCharacter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpringDataCharacterRepository extends JpaRepository<Character, Long> {
    List<Character> findByStoryIdOrderByFirstNameAsc(Long storyId);
    List<Character> findByStoryId(Long storyId);

    List<StoryCharacter> findByStoryIdOrderByNameAsc(Long storyId);

}