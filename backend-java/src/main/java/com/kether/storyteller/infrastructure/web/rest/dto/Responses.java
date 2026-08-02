package com.kether.storyteller.infrastructure.web.rest.dto;

import com.kether.storyteller.domain.entity.*;
import java.util.List;
import java.util.Map;

public final class Responses {

    // ══════════════════════════════════════════════════════════════
    // Story
    // ══════════════════════════════════════════════════════════════
    public record StoryResponse(Long id, String title, String synopsis, String blurb) {
        public static StoryResponse from(Story s) {
            return new StoryResponse(s.getId(), s.getTitle(), s.getSynopsis(), s.getBlurb());
        }
    }

    // ══════════════════════════════════════════════════════════════
    // Character
    // ══════════════════════════════════════════════════════════════
    public record CharacterResponse(
            Long id, Long storyId,
            String name, String surname, String role,
            Integer age, String born,
            String physicalDescription, String personality, String history,
            String motivation, String goal, String flaw,
            String characterArc, String skills, String notes
    ) {
        public static CharacterResponse from(StoryCharacter c) {
            return new CharacterResponse(
                    c.getId(), c.getStory().getId(),
                    c.getName(), c.getSurname(), c.getRole(),
                    c.getAge(), c.getBorn(),
                    c.getPhysicalDescription(), c.getPersonality(), c.getHistory(),
                    c.getMotivation(), c.getGoal(), c.getFlaw(),
                    c.getCharacterArc(), c.getSkills(), c.getNotes()
            );
        }
    }

    // ══════════════════════════════════════════════════════════════
    // Location
    // ══════════════════════════════════════════════════════════════
    public record LocationResponse(Long id, Long storyId, String name, String type, String summary) {
        public static LocationResponse from(StoryLocation l) {
            return new LocationResponse(l.getId(), l.getStory().getId(),
                    l.getName(), l.getType(), l.getSummary());
        }
    }

    // ══════════════════════════════════════════════════════════════
    // LoreEntry
    // ══════════════════════════════════════════════════════════════
    public record LoreEntryResponse(Long id, Long storyId, String title, String category, String content) {
        public static LoreEntryResponse from(LoreEntry e) {
            return new LoreEntryResponse(e.getId(), e.getStory().getId(),
                    e.getTitle(), e.getCategory(), e.getContent());
        }
    }

    // ══════════════════════════════════════════════════════════════
    // TimelineEvent
    // ══════════════════════════════════════════════════════════════
    public record TimelineEventResponse(
            Long id, Long storyId,
            String title, String date, Integer sortOrder, String summary,
            Long locationId,
            List<Long> characters
    ) {
        public static TimelineEventResponse from(TimelineEvent e) {
            return new TimelineEventResponse(
                    e.getId(), e.getStory().getId(),
                    e.getTitle(), e.getDate(), e.getSortOrder(), e.getSummary(),
                    e.getLocation() != null ? e.getLocation().getId() : null,
                    e.getCharacterIds()
            );
        }
    }

    // ══════════════════════════════════════════════════════════════
    // Manuscript
    // ══════════════════════════════════════════════════════════════
    public record ManuscriptResponse(Long id, Long storyId, String title,
                                     Integer chapter, String text, String status) {
        public static ManuscriptResponse from(Manuscript m) {
            return new ManuscriptResponse(m.getId(), m.getStory().getId(),
                    m.getTitle(), m.getChapter(),
                    m.getText(), m.getStatus());
        }
    }

    // ══════════════════════════════════════════════════════════════
    // LLM Config
    // ══════════════════════════════════════════════════════════════
    public record LLMConfigResponse(
            String provider, String model,
            String apiKey,
            String ollamaUrl,
            double temperature,
            int maxTokens
    ) {}

    public record LLMHealthResponse(
            String status, String provider, String model,
            boolean configured, String message
    ) {}

    public record LLMTestResponse(boolean ok, String message) {}

    public record LLMSaveResponse(String status, String provider, String model) {}

    // ══════════════════════════════════════════════════════════════
    // LLM Generation
    // ══════════════════════════════════════════════════════════════
    public record GeneratedChapterResponse(
            boolean success, String text,
            Integer chapterNumber, String chapterTitle, int wordCount
    ) {}

    public record ContinuationResponse(boolean success, String continuation, int wordCount) {}

    public record RewriteResponse(boolean success, String original, String rewritten, String instruction) {}

    public record SuggestionsResponse(List<Map<String, Object>> suggestions) {}

    public record SceneSuggestion(String title, String description,
                                  List<String> characters, String impact) {}

    // ══════════════════════════════════════════════════════════════
    // AI Analysis
    // ══════════════════════════════════════════════════════════════
    public record CharacterLinkSuggestion(String type, List<String> pair, String reason) {}
    public record SuggestionsResult(List<CharacterLinkSuggestion> suggestions) {}

    public record TimelineConflict(Long eventId, Long characterId, String reason) {}
    public record ConflictsResult(List<TimelineConflict> conflicts) {}

    public record LoreMention(Long loreId, String title, String type, String info) {}
    public record ScriptConsistencyResult(Map<String, Object> mentions, List<LoreMention> loreMentions) {}

    public record BehaviorIssue(Long characterId, String charName, String actionFound,
                                String conflictingTrait, String context, String reason) {}
    public record BehaviorResult(List<BehaviorIssue> behaviorIssues) {}

    // ══════════════════════════════════════════════════════════════
    // Extraction
    // ══════════════════════════════════════════════════════════════
    public record ExtractedCharacter(
            String name, String surname, String role,
            Integer age, String physicalDescription, String personality,
            String motivation, double confidence
    ) {}

    public record ExtractedLocation(String name, String type, String summary, double confidence) {}

    public record ExtractedTimelineEvent(
            String title, String date, String summary,
            int sortOrder, List<String> characterNames,
            String locationName, double confidence
    ) {}

    public record ExtractedLore(String title, String category, String content, double confidence) {}

    public record ExtractionResult(
            List<ExtractedCharacter> characters,
            List<ExtractedLocation> locations,
            List<ExtractedTimelineEvent> timeline,
            List<ExtractedLore> lore,
            String rawResponse
    ) {}

    public record ValidationResult(String status, String itemType, Long id, String message) {}

    // ══════════════════════════════════════════════════════════════
    // Relationship Analysis
    // ══════════════════════════════════════════════════════════════
    public record CharacterRelationship(
            String character1, String character2,
            String type, String description,
            double confidence, String evidence
    ) {}

    public record RelationshipAnalysisResult(List<CharacterRelationship> relationships, String rawResponse) {}

    // ══════════════════════════════════════════════════════════════
    // NLP / Manuscript analysis
    // ══════════════════════════════════════════════════════════════
    public record NamedEntity(String text, String label, int start, int end, String sentence) {}

    public record ManuscriptAnalysis(
            Long id, String title, Integer chapter,
            String mode, String status,
            List<Map<String, Object>> summary,
            List<NamedEntity> entities,
            int textLength
    ) {}

    // ══════════════════════════════════════════════════════════════
    // Générique
    // ══════════════════════════════════════════════════════════════
    public record OkResponse(boolean success) {
        public static OkResponse ok() {
            return new OkResponse(true);
        }
    }
}