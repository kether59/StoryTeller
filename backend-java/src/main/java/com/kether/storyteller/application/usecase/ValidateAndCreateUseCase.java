package com.kether.storyteller.application.usecase;

import com.kether.storyteller.application.dto.ValidationRequest;
import com.kether.storyteller.application.dto.ValidationResult;
import com.kether.storyteller.domain.model.ExtractedCharacter;
import com.kether.storyteller.domain.model.ExtractedLocation;
import com.kether.storyteller.domain.model.ExtractedLore;
import com.kether.storyteller.domain.model.ExtractedTimelineEvent;
import com.kether.storyteller.domain.port.out.CharacterRepositoryPort;
import com.kether.storyteller.domain.port.out.LocationRepositoryPort;
import com.kether.storyteller.domain.port.out.LoreRepositoryPort;
import com.kether.storyteller.domain.port.out.TimelineRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * Orchestre la validation et la création des éléments extraits.
 * Convertit les réponses utilisateur (approuvé/rejeté) en actions de persistance.
 */
@Component
@RequiredArgsConstructor
public class ValidateAndCreateUseCase {

    private final CharacterRepositoryPort characterRepo;
    private final LocationRepositoryPort locationRepo;
    private final TimelineRepositoryPort timelineRepo;
    private final LoreRepositoryPort loreRepo;

    @Transactional
    public ValidationResult execute(ValidationRequest request) {
        // Si rejeté, arrêter immédiatement
        if (!request.approved()) {
            return ValidationResult.rejected(request.itemType(), "Élément rejeté par l'utilisateur");
        }

        Map<String, Object> itemData = request.itemData();

        try {
            return switch (request.itemType()) {
                case "character" -> createCharacter(itemData);
                case "location" -> createLocation(itemData);
                case "timeline" -> createTimeline(itemData);
                case "lore" -> createLore(itemData);
                default -> throw new IllegalArgumentException(
                        "Type d'élément non supporté : " + request.itemType());
            };
        } catch (IllegalArgumentException e) {
            return ValidationResult.error(request.itemType(), e.getMessage());
        } catch (Exception e) {
            return ValidationResult.error(request.itemType(), "Erreur lors de la création : " + e.getMessage());
        }
    }

    private ValidationResult createCharacter(Map<String, Object> data) {
        String name = str(data, "name");
        Integer age = parseAge(data.get("age"));
        double confidence = num(data, "confidence");

        ExtractedCharacter character = new ExtractedCharacter(
                name,
                str(data, "surname"),
                str(data, "role"),
                age,
                str(data, "physical_description"),
                str(data, "personality"),
                str(data, "motivation"),
                confidence
        );

        characterRepo.save(character);
        return ValidationResult.created("character", name);
    }

    private ValidationResult createLocation(Map<String, Object> data) {
        String name = str(data, "name");
        double confidence = num(data, "confidence");

        ExtractedLocation location = new ExtractedLocation(
                name,
                str(data, "type"),
                str(data, "summary"),
                confidence
        );

        locationRepo.save(location);
        return ValidationResult.created("location", name);
    }

    private ValidationResult createTimeline(Map<String, Object> data) {
        String title = str(data, "title");
        double confidence = num(data, "confidence");

        ExtractedTimelineEvent event = new ExtractedTimelineEvent(
                title,
                str(data, "date"),
                str(data, "summary"),
                data.get("sort_order") instanceof Number n ? n.intValue() : 0,
                data.get("character_names") instanceof java.util.List<?> l
                    ? l.stream().map(Object::toString).toList()
                    : java.util.List.of(),
                str(data, "location_name"),
                confidence
        );

        timelineRepo.save(event);
        return ValidationResult.created("timeline", title);
    }

    private ValidationResult createLore(Map<String, Object> data) {
        String title = str(data, "title");
        double confidence = num(data, "confidence");

        ExtractedLore lore = new ExtractedLore(
                title,
                str(data, "category"),
                str(data, "content"),
                confidence
        );

        loreRepo.save(lore);
        return ValidationResult.created("lore", title);
    }

    // ── Utilitaires ────────────────────────────────────────────

    private static String str(Map<String, Object> m, String key) {
        Object v = m.get(key);
        return v != null ? v.toString() : null;
    }

    private static double num(Map<String, Object> m, String key) {
        Object v = m.get(key);
        if (v instanceof Number n) return n.doubleValue();
        return 0.0;
    }

    private static Integer parseAge(Object raw) {
        if (raw == null) return null;
        if (raw instanceof Number) return ((Number) raw).intValue();
        String s = raw.toString();
        for (char c : s.toCharArray()) {
            if (Character.isDigit(c)) {
                return Integer.parseInt(String.valueOf(c));
            }
        }
        return null;
    }
}
