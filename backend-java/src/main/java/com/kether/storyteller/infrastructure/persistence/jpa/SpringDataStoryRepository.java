package com.kether.storyteller.infrastructure.persistence.jpa;

import com.kether.storyteller.infrastructure.persistence.jpa.entity.Story;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Interface Spring Data — détail technique JPA.
 *
 * FONCTIONNEMENT :
 * - C'est une interface Spring Data standard
 * - Elle reste dans infrastructure car c'est un détail de persistance
 * - Elle n'est JAMAIS injectée dans l'application ou le domaine
 * - Seul l'Adapter (ci-dessous) l'utilise
 */
@Repository
public interface SpringDataStoryRepository extends JpaRepository<Story, Long> {
}