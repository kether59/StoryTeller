package com.kether.storyteller.infrastructure.llm.parser;

import com.kether.storyteller.domain.model.ExtractedLocation;
import com.kether.storyteller.domain.port.out.llm.LocationExtractionParserPort;  // ✅ CORRIGÉ
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

@Component
public class JacksonLocationExtractionParser implements LocationExtractionParserPort {

    private final ObjectMapper mapper;

    public JacksonLocationExtractionParser(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public List<ExtractedLocation> parse(String jsonResponse) {
        List<ExtractedLocation> locations = new ArrayList<>();

        try {
            String cleaned = cleanJson(jsonResponse);
            JsonNode root = mapper.readTree(cleaned);
            JsonNode locationsNode = root.get("locations");

            if (locationsNode != null && locationsNode.isArray()) {
                for (JsonNode locNode : locationsNode) {
                    try {
                        String name = locNode.has("name") ? locNode.get("name").asText() : null;
                        String type = locNode.has("type") ? locNode.get("type").asText() : null;
                        String summary = locNode.has("summary") ? locNode.get("summary").asText() : null;
                        double confidence = locNode.has("confidence")
                                ? locNode.get("confidence").asDouble()
                                : 0.0;

                        if (name != null && !name.isBlank()) {
                            locations.add(new ExtractedLocation(name, type, summary, confidence));
                        }
                    } catch (IllegalArgumentException e) {
                        // Ignore les lieux invalides
                    }
                }
            }
        } catch (Exception e) {
            // Si le parsing échoue complètement, retourner une liste vide
        }

        return locations;
    }

    private static String cleanJson(String raw) {
        return raw.strip()
                .replaceAll("(?s)^```json\\s*", "")
                .replaceAll("(?s)^```\\s*", "")
                .replaceAll("(?s)\\s*```$", "")
                .strip();
    }
}