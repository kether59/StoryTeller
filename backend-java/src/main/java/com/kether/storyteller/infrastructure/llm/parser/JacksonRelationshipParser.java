package com.kether.storyteller.infrastructure.llm.parser;

import com.kether.storyteller.domain.model.CharacterRelationship;
import com.kether.storyteller.domain.port.out.llm.RelationshipParserPort;  // ✅ CORRIGÉ
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

@Component
public class JacksonRelationshipParser implements RelationshipParserPort {

    private final ObjectMapper mapper;

    public JacksonRelationshipParser(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public List<CharacterRelationship> parse(String jsonResponse) {
        List<CharacterRelationship> relationships = new ArrayList<>();

        try {
            String cleaned = cleanJson(jsonResponse);
            JsonNode root = mapper.readTree(cleaned);
            JsonNode relsNode = root.get("relationships");

            if (relsNode != null && relsNode.isArray()) {
                for (JsonNode relNode : relsNode) {
                    try {
                        String character1 = relNode.has("character_1") ? relNode.get("character_1").asText() : null;
                        String character2 = relNode.has("character_2") ? relNode.get("character_2").asText() : null;
                        String type = relNode.has("type") ? relNode.get("type").asText() : null;
                        String description = relNode.has("description") ? relNode.get("description").asText() : null;
                        double confidence = relNode.has("confidence")
                                ? relNode.get("confidence").asDouble()
                                : 0.0;
                        String evidence = relNode.has("evidence") ? relNode.get("evidence").asText() : null;

                        if (character1 != null && character2 != null) {
                            relationships.add(new CharacterRelationship(
                                    character1, character2, type, description, confidence, evidence
                            ));
                        }
                    } catch (IllegalArgumentException e) {
                        // Ignore les relations invalides
                    }
                }
            }
        } catch (Exception e) {
            // Si le parsing échoue, retourner une liste vide
        }

        return relationships;
    }

    private static String cleanJson(String raw) {
        return raw.strip()
                .replaceAll("(?s)^```json\\s*", "")
                .replaceAll("(?s)^```\\s*", "")
                .replaceAll("(?s)\\s*```$", "")
                .strip();
    }
}