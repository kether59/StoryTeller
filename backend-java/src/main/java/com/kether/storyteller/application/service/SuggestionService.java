package com.kether.storyteller.application.service;

import com.kether.storyteller.application.dto.SuggestionCommand;
import com.kether.storyteller.application.dto.SuggestionResult;
import com.kether.storyteller.domain.port.in.llm.SuggestNextSceneUseCase;
import com.kether.storyteller.domain.port.out.llm.LLMGenerationPort;
import com.kether.storyteller.domain.port.out.persistence.*;
import com.kether.storyteller.domain.service.PromptBuilder;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kether.storyteller.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class SuggestionService implements SuggestNextSceneUseCase {

    private static final Logger log = LoggerFactory.getLogger(SuggestionService.class);

    private final StoryRepositoryPort storyRepo;
    private final CharacterRepositoryPort characterRepo;
    private final LocationRepositoryPort locationRepo;
    private final TimelineEventRepositoryPort timelineRepo;
    private final LoreEntryRepositoryPort loreRepo;
    private final LLMGenerationPort llmPort;
    private final PromptBuilder promptBuilder;
    private final ObjectMapper mapper;

    public SuggestionService(StoryRepositoryPort storyRepo,
                             CharacterRepositoryPort characterRepo,
                             LocationRepositoryPort locationRepo,
                             TimelineEventRepositoryPort timelineRepo,
                             LoreEntryRepositoryPort loreRepo,
                             LLMGenerationPort llmPort,
                             PromptBuilder promptBuilder,
                             ObjectMapper mapper) {
        this.storyRepo = storyRepo;
        this.characterRepo = characterRepo;
        this.locationRepo = locationRepo;
        this.timelineRepo = timelineRepo;
        this.loreRepo = loreRepo;
        this.llmPort = llmPort;
        this.promptBuilder = promptBuilder;
        this.mapper = mapper;
    }

    @Override
    public SuggestionResult suggest(SuggestionCommand cmd) {
        log.info("suggestNextScene — storyId={}", cmd.storyId());

        var story = storyRepo.findById(cmd.storyId())
                .orElseThrow(() -> ResourceNotFoundException.of("Story", cmd.storyId()));

        var chars = characterRepo.findByStoryId(cmd.storyId());
        var locs = locationRepo.findByStoryId(cmd.storyId());
        var timeline = timelineRepo.findByStoryId(cmd.storyId());
        var lore = loreRepo.findByStoryId(cmd.storyId());

        String systemPrompt = promptBuilder.buildSystemPrompt(story, chars, locs, timeline, lore, null);
        String userPrompt = promptBuilder.buildSuggestionUserPrompt(cmd.currentSituation());

        try {
            String raw = llmPort.generate(systemPrompt, userPrompt, 2000);
            String cleaned = cleanJsonResponse(raw);

            Map<String, List<Map<String, Object>>> parsed = mapper.readValue(
                    cleaned, new TypeReference<>() {});

            var suggestions = parsed.getOrDefault("suggestions", List.of());
            log.info("Suggestions parsed — count={}", suggestions.size());
            return new SuggestionResult(suggestions);

        } catch (Exception e) {
            log.error("Error during suggestNextScene", e);
            return new SuggestionResult(List.of(
                    Map.of("title", "Réponse brute", "description", e.getMessage(),
                            "characters", List.of(), "impact", "")
            ));
        }
    }

    private String cleanJsonResponse(String raw) {
        if (raw == null) return "{\"suggestions\":[]}";
        return raw.strip()
                .replaceAll("(?s)^```json\\s*", "")
                .replaceAll("(?s)^```\\s*", "")
                .replaceAll("(?s)\\s*```$", "")
                .strip();
    }
}