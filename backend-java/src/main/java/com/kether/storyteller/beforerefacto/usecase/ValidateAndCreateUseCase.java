package com.kether.storyteller.beforerefacto.usecase;

import com.kether.storyteller.beforerefacto.ValidationRequest;
import com.kether.storyteller.beforerefacto.ValidationResult;
import com.kether.storyteller.domain.entity.LoreEntry;
import com.kether.storyteller.domain.entity.Story;
import com.kether.storyteller.domain.entity.StoryCharacter;
import com.kether.storyteller.domain.entity.StoryLocation;
import com.kether.storyteller.domain.entity.TimelineEvent;
import com.kether.storyteller.domain.port.out.persistence.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ValidateAndCreateUseCase {

    private final StoryRepositoryPort storyRepo;
    private final CharacterRepositoryPort characterRepo;
    private final LocationRepositoryPort locationRepo;
    private final LoreEntryRepositoryPort loreRepo;
    private final TimelineEventRepositoryPort timelineRepo;

    public ValidationResult execute(ValidationRequest request) {
        if (!request.approved()) {
            return ValidationResult.rejected(request.itemType(), "Élément rejeté par l'utilisateur");
        }
        return switch (request.itemType()) {
            case "character" -> validateAndCreateCharacter(request);
            case "location" -> validateAndCreateLocation(request);
            case "lore" -> validateAndCreateLore(request);
            case "timeline" -> validateAndCreateTimeline(request);
            case "manuscript" -> ValidationResult.error("manuscript",
                    "Création de manuscrit non supportée via validate-and-create");
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
        try {
            Story story = storyRepo.findById(req.storyId())
                    .orElseThrow(() -> new RuntimeException("Story not found: " + req.storyId()));

            Map<String, Object> data = req.itemData();
            String name = str(data, "name");
            if (name == null || name.isBlank()) {
                return ValidationResult.error("character", "Le nom du personnage est obligatoire");
            }

            boolean duplicate = characterRepo.findByStoryId(req.storyId()).stream()
                    .anyMatch(c -> name.equalsIgnoreCase(c.getName()));
            if (duplicate) {
                return ValidationResult.duplicate("character",
                        "Un personnage nommé « " + name + " » existe déjà");
            }

            StoryCharacter character = new StoryCharacter();
            character.setStory(story);
            character.setName(name);
            character.setSurname(str(data, "surname"));
            character.setRole(str(data, "role"));
            character.setAge(parseInteger(data.get("age")));
            character.setPhysicalDescription(
                    firstNonBlank(str(data, "physicalDescription"), str(data, "physical_description")));
            character.setPersonality(str(data, "personality"));
            character.setMotivation(str(data, "motivation"));
            character.setHistory(str(data, "history"));
            character.setGoal(str(data, "goal"));
            character.setFlaw(str(data, "flaw"));
            character.setCharacterArc(
                    firstNonBlank(str(data, "characterArc"), str(data, "character_arc")));
            character.setSkills(str(data, "skills"));
            character.setNotes(str(data, "notes"));
            character.setBorn(str(data, "born"));

            StoryCharacter saved = characterRepo.save(character);
            return ValidationResult.created("character", saved.getId());
        } catch (Exception e) {
            return ValidationResult.error("character", e.getMessage());
        }
    }

    private ValidationResult validateAndCreateLocation(ValidationRequest req) {
        try {
            Story story = storyRepo.findById(req.storyId())
                    .orElseThrow(() -> new RuntimeException("Story not found: " + req.storyId()));
            Map<String, Object> data = req.itemData();
            String name = str(data, "name");
            if (name == null || name.isBlank()) {
                return ValidationResult.error("location", "Le nom du lieu est obligatoire");
            }

            StoryLocation location = new StoryLocation();
            location.setStory(story);
            location.setName(name);
            location.setType(str(data, "type"));
            location.setSummary(firstNonBlank(
                    str(data, "summary"),
                    str(data, "description"),
                    str(data, "physicalDescription")));

            StoryLocation saved = locationRepo.save(location);
            return ValidationResult.created("location", saved.getId());
        } catch (Exception e) {
            return ValidationResult.error("location", e.getMessage());
        }
    }

    private ValidationResult validateAndCreateLore(ValidationRequest req) {
        try {
            Story story = storyRepo.findById(req.storyId())
                    .orElseThrow(() -> new RuntimeException("Story not found: " + req.storyId()));
            Map<String, Object> data = req.itemData();
            String title = firstNonBlank(str(data, "title"), str(data, "name"));
            if (title == null || title.isBlank()) {
                return ValidationResult.error("lore", "Le titre de l'entrée de lore est obligatoire");
            }

            LoreEntry lore = new LoreEntry();
            lore.setStory(story);
            lore.setTitle(title);
            lore.setContent(firstNonBlank(str(data, "content"), str(data, "description")));
            lore.setCategory(str(data, "category"));

            LoreEntry saved = loreRepo.save(lore);
            return ValidationResult.created("lore", saved.getId());
        } catch (Exception e) {
            return ValidationResult.error("lore", e.getMessage());
        }
    }

    private ValidationResult validateAndCreateTimeline(ValidationRequest req) {
        try {
            Story story = storyRepo.findById(req.storyId())
                    .orElseThrow(() -> new RuntimeException("Story not found: " + req.storyId()));
            Map<String, Object> data = req.itemData();
            String title = firstNonBlank(str(data, "title"), str(data, "name"), str(data, "event"));
            if (title == null || title.isBlank()) {
                return ValidationResult.error("timeline", "Le titre de l'événement est obligatoire");
            }

            TimelineEvent event = new TimelineEvent();
            event.setStory(story);
            event.setTitle(title);
            event.setSummary(firstNonBlank(str(data, "summary"), str(data, "description")));
            event.setDate(firstNonBlank(
                    str(data, "date"),
                    str(data, "dateLabel"),
                    str(data, "when")));

            TimelineEvent saved = timelineRepo.save(event);
            return ValidationResult.created("timeline", saved.getId());
        } catch (Exception e) {
            return ValidationResult.error("timeline", e.getMessage());
        }
    }

    private static String str(Map<String, Object> m, String key) {
        Object v = m.get(key);
        return v != null ? v.toString().trim() : null;
    }

    private static String firstNonBlank(String... values) {
        if (values == null) return null;
        for (String v : values) {
            if (v != null && !v.isBlank()) return v;
        }
        return null;
    }

    private static Integer parseInteger(Object raw) {
        if (raw == null) return null;
        if (raw instanceof Number n) return n.intValue();
        try {
            var matcher = java.util.regex.Pattern.compile("-?\\d+").matcher(raw.toString());
            return matcher.find() ? Integer.parseInt(matcher.group()) : null;
        } catch (Exception e) {
            return null;
        }
    }
}
