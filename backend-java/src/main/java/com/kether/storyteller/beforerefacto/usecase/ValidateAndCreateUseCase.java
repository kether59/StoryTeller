package com.kether.storyteller.beforerefacto.usecase;

import com.kether.storyteller.domain.entity.*;
import com.kether.storyteller.domain.port.out.persistence.*;
import com.kether.storyteller.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ValidateAndCreateUseCase {

    private final StoryRepositoryPort storyRepo;
    private final CharacterRepositoryPort characterRepo;
    private final LocationRepositoryPort locationRepo;
    private final LoreEntryRepositoryPort loreRepo;
    private final TimelineEventRepositoryPort timelineRepo;
    private final ManuscriptRepositoryPort manuscriptRepo;

    public List<String> validateStory(Long storyId) {
        Story story = storyRepo.findById(storyId)
                .orElseThrow(() -> ResourceNotFoundException.of("Story", storyId));

        List<String> issues = new ArrayList<>();

        if (story.getTitle() == null || story.getTitle().isBlank()) {
            issues.add("Le titre de l'histoire est vide");
        }

        long charCount = characterRepo.findByStoryId(storyId).size();
        long locCount = locationRepo.findByStoryId(storyId).size();
        long loreCount = loreRepo.findByStoryId(storyId).size();

        if (charCount == 0) issues.add("Aucun personnage défini");
        if (locCount == 0) issues.add("Aucun lieu défini");
        if (loreCount == 0) issues.add("Aucun élément de lore défini");

        return issues;
    }

    public StoryCharacter validateAndCreateCharacter(Long storyId, String name, String role) {
        Story story = storyRepo.findById(storyId)
                .orElseThrow(() -> ResourceNotFoundException.of("Story", storyId));

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Le nom du personnage est requis");
        }

        StoryCharacter character = new StoryCharacter();
        character.setStory(story);
        character.setName(name);
        character.setRole(role);
        return characterRepo.save(character);
    }

    public StoryLocation validateAndCreateLocation(Long storyId, String name, String type) {
        Story story = storyRepo.findById(storyId)
                .orElseThrow(() -> ResourceNotFoundException.of("Story", storyId));

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Le nom du lieu est requis");
        }

        StoryLocation location = new StoryLocation();
        location.setStory(story);
        location.setName(name);
        location.setType(type);
        return locationRepo.save(location);
    }
}