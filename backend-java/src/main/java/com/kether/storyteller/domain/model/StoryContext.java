package com.kether.storyteller.domain.model;

import com.kether.storyteller.infrastructure.persistence.jpa.entity.LoreEntry;
import com.kether.storyteller.infrastructure.persistence.jpa.entity.StoryCharacter;
import com.kether.storyteller.infrastructure.persistence.jpa.entity.StoryLocation;
import com.kether.storyteller.infrastructure.persistence.jpa.entity.TimelineEvent;

import java.util.List;

/**
 * Contexte agrégé d'une story contenant tous les éléments nécessaires pour le contexte LLM.
 * Agrège les données de personnages, lieux, événements temporels et lore.
 */
public record StoryContext(
    List<StoryCharacter> characters,
    List<StoryLocation> locations,
    List<TimelineEvent> timelineEvents,
    List<LoreEntry> loreEntries
) {

    public static StoryContext empty() {
        return new StoryContext(List.of(), List.of(), List.of(), List.of());
    }
}
