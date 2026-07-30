package com.kether.storyteller.infrastructure.persistence.repository;

import com.kether.storyteller.infrastructure.persistence.entity.Story;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StoryRepository extends JpaRepository<Story, Long> {}
