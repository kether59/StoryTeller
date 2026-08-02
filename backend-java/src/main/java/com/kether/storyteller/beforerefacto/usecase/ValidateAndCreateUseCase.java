package com.kether.storyteller.beforerefacto.usecase;

import com.kether.storyteller.beforerefacto.ValidationRequest;
import com.kether.storyteller.beforerefacto.ValidationResult;
import com.kether.storyteller.domain.entity.*;
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
    private final ManuscriptRepositoryPort manuscriptRepo;

    public ValidationResult execute(ValidationRequest request) {
        return switch (request.itemType()) {
            case "character" -> validateAndCreateCharacter(request);
            case "location" -> validateAndCreateLocation(request);
            case "lore" -> validateAndCreateLore(request);
            case "timeline" -> validateAndCreateTimeline(request);
            case "manuscript" -> validateAndCreateManuscript(request);
            default -> new ValidationResult("error", request.itemType(), request.id(), "Type inconnu");
        };
    }

    public List<String> validateStory(Long storyId) {
        Story story = storyRepo.findById(storyId)
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
        // TODO: implémenter la validation métier réelle
        return new ValidationResult("ok", req.itemType(), req.id(), "Personnage validé et créé");
    }

    private ValidationResult validateAndCreateLocation(ValidationRequest req) {
        return new ValidationResult("ok", req.itemType(), req.id(), "Lieu validé et créé");
    }

    private ValidationResult validateAndCreateLore(ValidationRequest req) {
        return new ValidationResult("ok", req.itemType(), req.id(), "Lore validé et créé");
    }

    private ValidationResult validateAndCreateTimeline(ValidationRequest req) {
        return new ValidationResult("ok", req.itemType(), req.id(), "Événement validé et créé");
    }

    private ValidationResult validateAndCreateManuscript(ValidationRequest req) {
        return new ValidationResult("ok", req.itemType(), req.id(), "Manuscrit validé et créé");
    }
}