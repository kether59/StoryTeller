package com.kether.storyteller.service;

import com.kether.storyteller.dto.request.Requests.StoryCreate;
import  com.kether.storyteller.dto.request.Requests.StoryUpdate;
import  com.kether.storyteller.dto.response.Responses.StoryResponse;
import  com.kether.storyteller.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests d'intégration pour StoryService.
 * Utilise H2 en mémoire – aucun LLM requis.
 *
 * Équivalent Python : tests/test_stories.py avec TestClient FastAPI.
 */
@SpringBootTest
@Transactional
@ActiveProfiles("test")
class StoryServiceTest {

    @Autowired
    private StoryService storyService;

    // ── CRUD de base ──────────────────────────────────────────────

    @Test
    void createStory_shouldPersistAndReturn() {
        var req = new StoryCreate("Le Sceptre des Échos",
                "Un monde où la mémoire est monnaie d'échange", "Osez vous souvenir.");

        StoryResponse result = storyService.create(req);

        assertThat(result.id()).isNotNull();
        assertThat(result.title()).isEqualTo("Le Sceptre des Échos");
        assertThat(result.synopsis()).contains("mémoire");
    }

    @Test
    void listStories_shouldReturnAllCreated() {
        storyService.create(new StoryCreate("Histoire 1", null, null));
        storyService.create(new StoryCreate("Histoire 2", null, null));

        List<StoryResponse> list = storyService.findAll();

        assertThat(list).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    void updateStory_shouldModifyFields() {
        StoryResponse created = storyService.create(
                new StoryCreate("Titre original", "Synopsis", null));

        StoryResponse updated = storyService.update(
                created.id(),
                new StoryUpdate("Nouveau titre", null, "Quatrième de couverture"));

        assertThat(updated.title()).isEqualTo("Nouveau titre");
        assertThat(updated.synopsis()).isEqualTo("Synopsis");   // inchangé
        assertThat(updated.blurb()).isEqualTo("Quatrième de couverture");
    }

    @Test
    void deleteStory_shouldRemoveFromDatabase() {
        StoryResponse created = storyService.create(
                new StoryCreate("À supprimer", null, null));

        storyService.delete(created.id());

        assertThatThrownBy(() -> storyService.findById(created.id()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Histoire");
    }

    @Test
    void findByIdUnknown_shouldThrowResourceNotFoundException() {
        assertThatThrownBy(() -> storyService.findById(99999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99999");
    }
}