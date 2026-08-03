package com.kether.storyteller.infrastructure.llm.parser;

import com.kether.storyteller.domain.model.ExtractedTimelineEvent;
import com.kether.storyteller.domain.port.out.llm.TimelineExtractionParserPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class JacksonTimelineExtractionParser implements TimelineExtractionParserPort {

    private final ObjectMapper mapper;

    public JacksonTimelineExtractionParser(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public List<ExtractedTimelineEvent> parse(String jsonResponse) {
        List<ExtractedTimelineEvent> events = new ArrayList<>();

        try {
            String cleaned = cleanJson(jsonResponse);
            JsonNode root = mapper.readTree(cleaned);
            JsonNode timelineNode = root.get("timeline");

            if (timelineNode != null && timelineNode.isArray()) {
                for (JsonNode eventNode : timelineNode) {
                    try {
                        String title = eventNode.has("title") ? eventNode.get("title").asText() : null;
                        String date = eventNode.has("date") ? eventNode.get("date").asText() : null;
                        String summary = eventNode.has("summary") ? eventNode.get("summary").asText() : null;
                        Integer sortOrder = eventNode.has("sort_order")
                                ? eventNode.get("sort_order").asInt()
                                : 0;

                        List<String> characterNames = new ArrayList<>();
                        if (eventNode.has("character_names") && eventNode.get("character_names").isArray()) {
                            for (JsonNode charName : eventNode.get("character_names")) {
                                characterNames.add(charName.asText());
                            }
                        }

                        String locationName = eventNode.has("location_name")
                                ? eventNode.get("location_name").asText()
                                : null;

                        double confidence = eventNode.has("confidence")
                                ? eventNode.get("confidence").asDouble()
                                : 0.0;

                        if (title != null && !title.isBlank()) {
                            events.add(new ExtractedTimelineEvent(
                                    title, date, summary, sortOrder, characterNames, locationName, confidence
                            ));
                        }
                    } catch (IllegalArgumentException e) {
                        log.info("Ignore les événements invalides");
                    }
                }
            }
        } catch (Exception e) {
            log.info("Si le parsing échoue, retourner une liste vide");
        }

        return events;
    }

    private static String cleanJson(String raw) {
        return raw.strip()
                .replaceAll("(?s)^```json\\s*", "")
                .replaceAll("(?s)^```\\s*", "")
                .replaceAll("(?s)\\s*```$", "")
                .strip();
    }
}