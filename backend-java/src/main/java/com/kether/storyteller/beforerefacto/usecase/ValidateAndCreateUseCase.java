package com.kether.storyteller.beforerefacto.usecase;

import com.kether.storyteller.beforerefacto.ValidationRequest;
import com.kether.storyteller.beforerefacto.ValidationResult;
import com.kether.storyteller.domain.port.out.persistence.*;
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

    public ValidationResult execute(ValidationRequest request) {
        return switch (request.itemType()) {
            case "character" -> validateAndCreateCharacter(request);
            case "location" -> validateAndCreateLocation(request);
            case "lore" -> validateAndCreateLore(request);
            case "timeline" -> validateAndCreateTimeline(request);
            case "manuscript" -> validateAndCreateManuscript(request);
            default -> ValidationResult.error(request.itemType(), "Type inconnu : " + request.itemType());
        };
    }

    public List<String> validateStory(Long storyId) {
        var story = storyRepo.findById(storyId)
                .orElseThrow(() -> new RuntimeException("Story not found: " + storyId));

        List<String> issues = new ArrayList<>();
        if (story.getTitle() == null || story.getTitle().isBlank()) {
            issues.add("Le titre de l'histoire est vide");
        }
        if (characterRepo.findByStoryId(storyId).isEmpty()) issues.add("Aucun personnage défini");
        if (locationRepo.findByStoryId(storyId).isEmpty()) issues.add("Aucun lieu défini");
        if (loreRepo.findByStoryId(storyId).isEmpty()) issues.add("Aucun élément de lore défini");

        return issues;
    }

    private ValidationResult validateAndCreateCharacter(ValidationRequest req) {
        // TODO: implémenter validation métier + création depuis req.itemData()
        return ValidationResult.created(req.itemType(), null);
    }

    private ValidationResult validateAndCreateLocation(ValidationRequest req) {
        return ValidationResult.created(req.itemType(), null);
    }

    private ValidationResult validateAndCreateLore(ValidationRequest req) {
        return ValidationResult.created(req.itemType(), null);
    }

    private ValidationResult validateAndCreateTimeline(ValidationRequest req) {
        return ValidationResult.created(req.itemType(), null);
    }

    private ValidationResult validateAndCreateManuscript(ValidationRequest req) {
        return ValidationResult.created(req.itemType(), null);
    }
}