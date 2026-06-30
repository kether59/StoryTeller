package com.kether.storyteller;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

/**
 * Test de démarrage du contexte Spring.
 * Équivalent d'un test de santé basique.
 *
 * Utilise une base H2 en mémoire pour les tests
 * (évite la dépendance SQLite en CI).
 */
@SpringBootTest
@ActiveProfiles("test")
class StoryTellerApplicationTests {

    @Test
    void contextLoads() {
        // Si ce test passe, Spring Boot a démarré sans erreur
    }
}