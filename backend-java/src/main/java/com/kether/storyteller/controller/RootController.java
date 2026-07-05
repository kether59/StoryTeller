package com.kether.storyteller.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Endpoint racine – équivalent Python :
 * <pre>
 *   @app.get("/")
 *   def root():
 *       return {"message": "StoryTeller API", "version": "2.0", ...}
 * </pre>
 */
@RestController
public class RootController {

    @GetMapping("/")
    public Map<String, Object> root() {
        return Map.of(
                "message",  "StoryTeller API",
                "version",  "2.0",
                "language", "Java 25 / Spring Boot 4.0.1",
                "features", List.of("AI Analysis", "LLM Writing Assistant",
                        "NLP Extraction", "Virtual Threads"),
                "endpoints", Map.of(
                        "stories",    "/api/stories",
                        "characters", "/api/characters",
                        "locations",  "/api/locations",
                        "lore",       "/api/lore",
                        "timeline",   "/api/timeline",
                        "manuscript", "/api/manuscript",
                        "llm",        "/api/llm",
                        "ai",         "/api/ai",
                        "extraction", "/api/extraction",
                        "health",     "/actuator/health"
                )
        );
    }
}