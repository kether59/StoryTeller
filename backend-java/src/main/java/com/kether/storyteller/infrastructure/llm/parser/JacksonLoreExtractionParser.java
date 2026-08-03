package com.kether.storyteller.infrastructure.llm.parser;

import com.kether.storyteller.domain.model.ExtractedLore;
import com.kether.storyteller.domain.port.out.llm.LoreExtractionParserPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;


@Component
@Slf4j
public class JacksonLoreExtractionParser implements LoreExtractionParserPort {

    private final ObjectMapper mapper;


    public JacksonLoreExtractionParser(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public List<ExtractedLore> parse(String jsonResponse) {
        List<ExtractedLore> loreEntries = new ArrayList<>();

        try {
            String cleaned = cleanJson(jsonResponse);
            JsonNode root = mapper.readTree(cleaned);
            JsonNode loreNode = root.get("lore");

            if (loreNode != null && loreNode.isArray()) {
                for (JsonNode entryNode : loreNode) {
                    try {
                        String title = entryNode.has("title") ? entryNode.get("title").asText() : null;
                        String category = entryNode.has("category") ? entryNode.get("category").asText() : null;
                        String content = entryNode.has("content") ? entryNode.get("content").asText() : null;
                        double confidence = entryNode.has("confidence")
                                ? entryNode.get("confidence").asDouble()
                                : 0.0;

                        if (title != null && !title.isBlank()) {
                            loreEntries.add(new ExtractedLore(title, category, content, confidence));
                        }
                    } catch (IllegalArgumentException e) {
                        log.info("Ignore les entrées invalides");
                    }
                }
            }
        } catch (Exception e) {
            log.info("Si le parsing échoue, retourner une liste vide");
        }

        return loreEntries;
    }

    private static String cleanJson(String raw) {
        return raw.strip()
                .replaceAll("(?s)^```json\\s*", "")
                .replaceAll("(?s)^```\\s*", "")
                .replaceAll("(?s)\\s*```$", "")
                .strip();
    }
}