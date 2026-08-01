package com.kether.storyteller.infrastructure.llm.parser;

import com.kether.storyteller.domain.model.TimelineConflict;
import com.kether.storyteller.domain.port.out.TimelineConflictParserPort;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

/**
 * Implémentation Jackson du parser pour les conflits de chronologie.
 */
@Component
public class JacksonTimelineConflictParser implements TimelineConflictParserPort {

    private final ObjectMapper mapper;

    public JacksonTimelineConflictParser(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public List<TimelineConflict> parse(String jsonResponse) {
        List<TimelineConflict> conflicts = new ArrayList<>();
        
        try {
            String cleaned = cleanJson(jsonResponse);
            JsonNode root = mapper.readTree(cleaned);
            JsonNode conflictsNode = root.get("conflicts");
            
            if (conflictsNode != null && conflictsNode.isArray()) {
                for (JsonNode conflictNode : conflictsNode) {
                    try {
                        String type = conflictNode.has("type") ? conflictNode.get("type").asText() : null;
                        String description = conflictNode.has("description") ? conflictNode.get("description").asText() : null;
                        String event1 = conflictNode.has("event_1") ? conflictNode.get("event_1").asText() : null;
                        String event2 = conflictNode.has("event_2") ? conflictNode.get("event_2").asText() : null;
                        String suggestion = conflictNode.has("suggestion") ? conflictNode.get("suggestion").asText() : null;
                        double severity = conflictNode.has("severity") 
                            ? conflictNode.get("severity").asDouble() 
                            : 0.0;
                        
                        if (type != null && !type.isBlank()) {
                            conflicts.add(new TimelineConflict(
                                type, description, event1, event2, suggestion, severity
                            ));
                        }
                    } catch (IllegalArgumentException e) {
                        // Ignore les conflits invalides
                    }
                }
            }
        } catch (Exception e) {
            // Si le parsing échoue, retourner une liste vide
        }
        
        return conflicts;
    }

    private static String cleanJson(String raw) {
        return raw.strip()
                .replaceAll("(?s)^```json\\s*", "")
                .replaceAll("(?s)^```\\s*", "")
                .replaceAll("(?s)\\s*```$", "")
                .strip();
    }
}
