package com.kether.storyteller.service;

import com.kether.storyteller.dto.request.Requests.*;
import com.kether.storyteller.dto.response.Responses.*;
import com.kether.storyteller.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CharacterServiceTest {

    @Autowired StoryService     storyService;
    @Autowired CharacterService characterService;
    @Autowired LocationService  locationService;
    @Autowired TimelineService  timelineService;

    private Long storyId;

    @BeforeEach
    void setup() {
        StoryResponse story = storyService.create(
                new StoryCreate("Test Story", "Synopsis de test", null));
        storyId = story.id();
    }

    // ── Personnages ───────────────────────────────────────────────

    @Test
    void createCharacter_shouldLinkToStory() {
        var req = new CharacterCreate(
                storyId, "Elara", "Vancian", "Protagoniste",
                25, "1024-06-27",
                "Petite, agile", "Sceptique", "Histoire...",
                "Démasquer l'Ordre", "Trouver le sceptre",
                "Trop confiante", "Du marchand à la leader",
                "Combat, commerce", "Note"
        );

        CharacterResponse result = characterService.create(req);

        assertThat(result.id()).isNotNull();
        assertThat(result.storyId()).isEqualTo(storyId);
        assertThat(result.name()).isEqualTo("Elara");
        assertThat(result.age()).isEqualTo(25);
    }

    @Test
    void listCharactersByStory_shouldReturnOnlyForStory() {
        characterService.create(new CharacterCreate(storyId, "Elara", null,
                null, null, null, null, null, null, null, null, null, null, null, null));
        characterService.create(new CharacterCreate(storyId, "Kellan", null,
                null, null, null, null, null, null, null, null, null, null, null, null));

        List<CharacterResponse> list = characterService.findByStory(storyId);

        assertThat(list).hasSize(2);
        assertThat(list).extracting(CharacterResponse::name)
                .containsExactlyInAnyOrder("Elara", "Kellan");
    }

    @Test
    void updateCharacter_shouldModifyFields() {
        CharacterResponse created = characterService.create(
                new CharacterCreate(storyId, "Elara", null, "Protagoniste",
                        25, null, null, null, null, null, null, null, null, null, null));

        CharacterResponse updated = characterService.update(
                created.id(),
                new CharacterUpdate(null, "Vancian", "Héroïne",
                        26, null, null, null, null, null, null, null, null, null, null));

        assertThat(updated.surname()).isEqualTo("Vancian");
        assertThat(updated.role()).isEqualTo("Héroïne");
        assertThat(updated.age()).isEqualTo(26);
        assertThat(updated.name()).isEqualTo("Elara");  // inchangé
    }

    @Test
    void deleteCharacter_shouldRemove() {
        CharacterResponse c = characterService.create(
                new CharacterCreate(storyId, "À supprimer", null,
                        null, null, null, null, null, null, null, null, null, null, null, null));

        characterService.delete(c.id());

        assertThatThrownBy(() -> characterService.findById(c.id()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── Timeline ──────────────────────────────────────────────────

    @Test
    void createTimelineEvent_shouldPersistWithOrder() {
        var req = new TimelineEventCreate(storyId, "Découverte du Sceptre",
                "2024-01-01", 100, "Elara trouve le sceptre", null, List.of());

        TimelineEventResponse result = timelineService.create(req);

        assertThat(result.id()).isNotNull();
        assertThat(result.storyId()).isEqualTo(storyId);
        assertThat(result.sortOrder()).isEqualTo(100);
    }

    @Test
    void timelineEvents_shouldReturnInSortOrder() {
        timelineService.create(new TimelineEventCreate(storyId, "Événement 3",
                null, 300, null, null, List.of()));
        timelineService.create(new TimelineEventCreate(storyId, "Événement 1",
                null, 100, null, null, List.of()));
        timelineService.create(new TimelineEventCreate(storyId, "Événement 2",
                null, 200, null, null, List.of()));

        List<TimelineEventResponse> events = timelineService.findByStory(storyId);

        assertThat(events).extracting(TimelineEventResponse::title)
                .containsExactly("Événement 1", "Événement 2", "Événement 3");
    }

    @Test
    void timelineEvent_shouldLinkCharacters() {
        CharacterResponse elara = characterService.create(
                new CharacterCreate(storyId, "Elara", null, null, null, null,
                        null, null, null, null, null, null, null, null, null));

        TimelineEventResponse event = timelineService.create(
                new TimelineEventCreate(storyId, "Confrontation", "2024-01-03",
                        120, "Elara vs Kellan", null, List.of(elara.id())));

        assertThat(event.characters()).contains(elara.id());
    }

    // ── Lieux ─────────────────────────────────────────────────────

    @Test
    void createLocation_shouldPersist() {
        var req = new LocationCreate(storyId, "Aethel", "Capitale", "Ville de brume");

        LocationResponse result = locationService.create(req);

        assertThat(result.id()).isNotNull();
        assertThat(result.name()).isEqualTo("Aethel");
        assertThat(result.type()).isEqualTo("Capitale");
    }
}