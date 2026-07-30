package com.kether.storyteller.domain.service;

import com.kether.storyteller.domain.model.ExtractedCharacter;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class CharacterExtractionParser {

    private final ObjectMapper mapper;

    public CharacterExtractionParser(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public List<ExtractedCharacter> parse(String rawJson) {
        try {
            String cleaned = rawJson.strip()
                    .replaceAll("(?s)^```json\\s*", "")
                    .replaceAll("(?s)^```\\s*", "")
                    .replaceAll("(?s)\\s*```$", "")
                    .strip();

            Map<String, Object> data = mapper.readValue(cleaned, new TypeReference<>() {});

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> list = (List<Map<String, Object>>) data.getOrDefault("characters", List.of());

            return list.stream().map(this::toCharacter).toList();

        } catch (Exception e) {
            throw new RuntimeException("Impossible de parser la réponse LLM", e);
        }
    }

    private ExtractedCharacter toCharacter(Map<String, Object> m) {
        return new ExtractedCharacter(
                str(m, "name"),
                str(m, "surname"),
                str(m, "role"),
                parseAge(m.get("age")),
                str(m, "physical_description"),
                str(m, "personality"),
                str(m, "motivation"),
                num(m, "confidence")
        );
    }

    private static String str(Map<String, Object> m, String key) {
        Object v = m.get(key);
        return v != null ? v.toString() : null;
    }

    private static double num(Map<String, Object> m, String key) {
        Object v = m.get(key);
        return v instanceof Number n ? n.doubleValue() : 0.0;
    }

    private static Integer parseAge(Object raw) {
        if (raw == null) return null;
        if (raw instanceof Number n) return n.intValue();
        var matcher = java.util.regex.Pattern.compile("\\d+").matcher(raw.toString());
        return matcher.find() ? Integer.parseInt(matcher.group()) : null;
    }
}