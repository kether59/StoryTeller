package com.kether.storyteller.infrastructure.persistence.jpa;

import com.kether.storyteller.domain.entity.Story;
import org.springframework.data.jpa.repository.JpaRepository;

public class SpringDataCharacterRepository extends JpaRepository<Story, Long> {
}