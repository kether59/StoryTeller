package com.kether.storyteller.beforerefacto.usecase;

import com.kether.storyteller.domain.model.TimelineConflict;
import com.kether.storyteller.domain.port.out.LLMPort;
import com.kether.storyteller.domain.port.out.TimelineConflictParserPort;
import com.kether.storyteller.domain.entity.StoryCharacter;
import com.kether.storyteller.domain.entity.TimelineEvent;
import com.kether.storyteller.repository.TimelineEventRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Use Case : Trouver les conflits dans la chronologie.
 * Vérifie les incohérences (personnage pas encore né lors d'un événement, etc.)
 */
@Component
@RequiredArgsConstructor
public class FindTimelineConflictsUseCase {

    private static final Logger log = LoggerFactory.getLogger(FindTimelineConflictsUseCase.class);

    private final TimelineEventRepository timelineRepo;
    private final LLMPort llmPort;
    private final TimelineConflictParserPort parser;

    public List<TimelineConflict> execute(Long storyId) {
        List<TimelineConflict> conflicts = new ArrayList<>();

        // 1. Vérifications simples (dates de naissance)
        var events = timelineRepo.findByStoryIdWithCharacters(storyId);
        
        for (TimelineEvent event : events) {
            if (event.getDate() == null) continue;

            LocalDate eventDate = parseDate(event.getDate());
            if (eventDate == null) continue;

            for (StoryCharacter character : event.getCharacters()) {
                if (character.getBorn() != null) {
                    LocalDate born = parseDate(character.getBorn());
                    if (born != null && born.isAfter(eventDate)) {
                        conflicts.add(new TimelineConflict(
                            "birth_date_conflict",
                            character.getName() + " n'est pas encore né(e)",
                            event.getTitle(),
                            "Né le " + character.getBorn(),
                            "Vérifier la date de naissance ou l'événement",
                            0.95
                        ));
                    }
                }
            }
        }

        log.info("Timeline conflicts found — count={}", conflicts.size());
        return conflicts;
    }

    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) return null;
        
        try {
            // Essai format ISO
            return LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException e1) {
            try {
                // Essai format français
                return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            } catch (DateTimeParseException e2) {
                return null;
            }
        }
    }
}
