package com.kether.storyteller.infrastructure.web.rest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * DTOs de requête – équivalent des schémas Pydantic Python (schemas.py).
 * On utilise des Java Records (immuables, compacts, Java 16+).
 */
public final class Requests {

    // ══════════════════════════════════════════════════════════════
    //  Story
    // ══════════════════════════════════════════════════════════════

    /** POST /api/stories  – équivalent StoryCreate */
    public record StoryCreate(
            @NotBlank String title,
            String synopsis,
            String blurb
    ) {}

    /** PUT /api/stories/{id}  – équivalent StoryUpdate */
    public record StoryUpdate(
            String title,
            String synopsis,
            String blurb
    ) {}

    // ══════════════════════════════════════════════════════════════
    //  Character
    // ══════════════════════════════════════════════════════════════

    public record CharacterCreate(
            @NotNull Long storyId,
            @NotBlank String name,
            String surname,
            String role,
            Integer age,
            String born,
            String physicalDescription,
            String personality,
            String history,
            String motivation,
            String goal,
            String flaw,
            String characterArc,
            String skills,
            String notes
    ) {}

    public record CharacterUpdate(
            String name,
            String surname,
            String role,
            Integer age,
            String born,
            String physicalDescription,
            String personality,
            String history,
            String motivation,
            String goal,
            String flaw,
            String characterArc,
            String skills,
            String notes
    ) {}

    // ══════════════════════════════════════════════════════════════
    //  Location
    // ══════════════════════════════════════════════════════════════

    public record LocationCreate(
            @NotNull Long storyId,
            String name,
            String type,
            String summary
    ) {}

    public record LocationUpdate(
            String name,
            String type,
            String summary
    ) {}

    // ══════════════════════════════════════════════════════════════
    //  LoreEntry
    // ══════════════════════════════════════════════════════════════

    public record LoreEntryCreate(
            @NotNull Long storyId,
            String title,
            String category,
            String content
    ) {}

    public record LoreEntryUpdate(
            String title,
            String category,
            String content
    ) {}

    // ══════════════════════════════════════════════════════════════
    //  TimelineEvent
    // ══════════════════════════════════════════════════════════════

    public record TimelineEventCreate(
            @NotNull Long storyId,
            String title,
            String date,
            Integer sortOrder,
            String summary,
            Long locationId,
            List<Long> characters
    ) {}

    public record TimelineEventUpdate(
            String title,
            String date,
            Integer sortOrder,
            String summary,
            Long locationId,
            List<Long> characters
    ) {}

    // ══════════════════════════════════════════════════════════════
    //  Manuscript
    // ══════════════════════════════════════════════════════════════

    public record ManuscriptCreate(
            @NotNull Long storyId,
            String title,
            Integer chapter,
            String text,
            String status
    ) {}

    public record ManuscriptUpdate(
            String title,
            Integer chapter,
            String text,
            String status
    ) {}

    // ══════════════════════════════════════════════════════════════
    //  LLM Config  – équivalent llm_config.py LLMConfig
    // ══════════════════════════════════════════════════════════════

    public record LLMConfigRequest(
            String provider,        // anthropic | openai | openrouter | ollama
            String model,
            String apiKey,
            String ollamaUrl,
            Double temperature,
            Integer maxTokens
    ) {}

    public record LLMTestRequest(
            @NotBlank String provider,
            String model,
            String apiKey,
            String ollamaUrl,
            String lmstudioUrl,
            String geminiApiKey
    ) {}

    // ══════════════════════════════════════════════════════════════
    //  LLM Writing  – équivalent llm.py request models
    // ══════════════════════════════════════════════════════════════

    public record ChapterGenerationRequest(
            @NotNull Long storyId,
            Integer chapterNumber,
            String chapterTitle,
            @NotBlank String summary,
            String style,           // narratif | dialogue | descriptif | action
            String length,          // court | moyen | long
            List<Long> includeCharacters,
            List<Long> includeLocations,
            String tone,            // neutre | dramatique | humoristique | sombre | léger
            String pov              // première personne | troisième personne | ...
    ) {}

    public record ContinueWritingRequest(
            @NotNull Long manuscriptId,
            @NotBlank String direction,
            Integer length
    ) {}

    public record RewriteRequest(
            @NotBlank String text,
            @NotBlank String instruction
    ) {}

    public record SuggestNextSceneRequest(
            @NotNull Long storyId,
            @NotBlank String currentSituation
    ) {}

    // ══════════════════════════════════════════════════════════════
    //  AI Analysis  – équivalent ai.py SuggestRequest
    // ══════════════════════════════════════════════════════════════

    public record AIAnalysisRequest(
            @NotBlank String intent,   // link_characters | timeline_conflicts | script_consistency | …
            @JsonProperty("manuscript_id")
            Long manuscriptId
    ) {}

    // ══════════════════════════════════════════════════════════════
    //  Extraction  – équivalent extraction.py ExtractionRequest
    // ══════════════════════════════════════════════════════════════

    public record ExtractionRequest(
            @NotNull
            @JsonProperty("manuscript_id")
            Long manuscriptId,
            @JsonProperty("extract_types")
            List<String> extractTypes  // characters | locations | timeline | lore
    ) {}

    public record ValidationRequest(
            @NotNull Long storyId,
            @NotBlank String itemType,  // character | location | timeline | lore
            java.util.Map<String, Object> itemData,
            boolean approved
    ) {}
}