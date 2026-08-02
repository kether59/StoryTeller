package com.kether.storyteller.controller;

import com.kether.storyteller.application.dto.LLMConfigDto;
import com.kether.storyteller.application.dto.LLMTestResultDto;
import com.kether.storyteller.domain.port.in.llm.*;
import com.kether.storyteller.infrastructure.llm.LLMHttpClient;
import com.kether.storyteller.infrastructure.web.rest.dto.Requests;
import com.kether.storyteller.infrastructure.web.rest.dto.Responses;
import com.kether.storyteller.service.llm.LLMConfigService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import com.fasterxml.jackson.databind.JsonNode;        // ✅ CORRIGÉ
import com.fasterxml.jackson.databind.ObjectMapper;   // ✅ CORRIGÉ

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/llm")
public class LLMController {

    private static final Logger log = LoggerFactory.getLogger(LLMController.class);

    private final GenerateChapterUseCase generateChapter;
    private final ContinueWritingUseCase continueWriting;
    private final RewriteUseCase rewrite;
    private final SuggestNextSceneUseCase suggestScene;
    private final ManageLLMConfigUseCase manageConfig;
    private final LLMConfigService configService;
    private final LLMHttpClient httpClient;
    private final ObjectMapper mapper;

    public LLMController(GenerateChapterUseCase generateChapter,
                         ContinueWritingUseCase continueWriting,
                         RewriteUseCase rewrite,
                         SuggestNextSceneUseCase suggestScene,
                         ManageLLMConfigUseCase manageConfig,
                         LLMConfigService configService,
                         LLMHttpClient httpClient,
                         ObjectMapper mapper) {
        this.generateChapter = generateChapter;
        this.continueWriting = continueWriting;
        this.rewrite = rewrite;
        this.suggestScene = suggestScene;
        this.manageConfig = manageConfig;
        this.configService = configService;
        this.httpClient = httpClient;
        this.mapper = mapper;
    }

    // ══════════════════════════════════════════════════════════════
    // Génération
    // ══════════════════════════════════════════════════════════════

    @PostMapping("/generate-chapter")
    public Responses.GeneratedChapterResponse generateChapter(@Valid @RequestBody Requests.ChapterGenerationRequest req) {
        var command = new com.kether.storyteller.application.dto.ChapterGenerationCommand(
                req.storyId(), req.chapterNumber(), req.chapterTitle(),
                req.summary(), req.includeCharacters(), req.includeLocations(), req.length());

        var result = generateChapter.generate(command);
        return new Responses.GeneratedChapterResponse(
                result.success(), result.text(), result.chapterNumber(),
                result.chapterTitle(), result.wordCount());
    }

    @PostMapping("/continue-writing")
    public Responses.ContinuationResponse continueWriting(@Valid @RequestBody Requests.ContinueWritingRequest req) {
        var command = new com.kether.storyteller.application.dto.ContinuationCommand(
                req.manuscriptId(), req.direction(), req.length());

        var result = continueWriting.continueWriting(command);
        return new Responses.ContinuationResponse(result.success(), result.text(), result.wordCount());
    }

    @PostMapping("/rewrite")
    public Responses.RewriteResponse rewrite(@Valid @RequestBody Requests.RewriteRequest req) {
        var command = new com.kether.storyteller.application.dto.RewriteCommand(
                req.text(), req.instruction());

        var result = rewrite.rewrite(command);
        return new Responses.RewriteResponse(result.success(), result.originalText(),
                result.rewrittenText(), result.instruction());
    }

    @PostMapping("/suggest-next-scene")
    public Responses.SuggestionsResponse suggestNextScene(@Valid @RequestBody Requests.SuggestNextSceneRequest req) {
        var command = new com.kether.storyteller.application.dto.SuggestionCommand(
                req.storyId(), req.currentSituation());

        var result = suggestScene.suggest(command);
        return new Responses.SuggestionsResponse(result.suggestions());
    }

    // ══════════════════════════════════════════════════════════════
    // Configuration
    // ══════════════════════════════════════════════════════════════

    @GetMapping("/config")
    public Responses.LLMConfigResponse getConfig() {
        var dto = manageConfig.getCurrentConfig();
        return new Responses.LLMConfigResponse(
                dto.provider(), dto.model(), maskKey(dto.apiKey()),
                dto.ollamaUrl(), dto.temperature(), dto.maxTokens()
        );
    }

    @PostMapping("/config")
    public Responses.LLMSaveResponse saveConfig(@RequestBody Requests.LLMConfigRequest req) {
        var dto = new LLMConfigDto(
                req.provider(), req.model(), req.apiKey(),
                req.ollamaUrl(), req.temperature(), req.maxTokens());
        manageConfig.updateConfig(dto);
        return new Responses.LLMSaveResponse("ok", dto.provider(), dto.model());
    }

    @PostMapping("/test")
    public Responses.LLMTestResponse testConnection(@Valid @RequestBody Requests.LLMTestRequest req) {
        LLMTestResultDto result = manageConfig.testConnection(
                req.provider(), req.model(), req.apiKey());
        return new Responses.LLMTestResponse(result.success(), result.message());
    }

    @GetMapping("/health")
    public Responses.LLMHealthResponse health() {
        return configService.getHealth();
    }

    // ══════════════════════════════════════════════════════════════
    // Local Models
    // ══════════════════════════════════════════════════════════════

    @GetMapping("/local/models")
    public List<String> getLocalModels(
            @RequestParam String url,
            @RequestParam(required = false) String provider,
            @RequestParam(required = false, defaultValue = "60") int timeoutSeconds) {

        String baseUrl = url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
        String endpoint = resolveModelsEndpoint(baseUrl, provider);

        log.info("Récupération modèles depuis {} (provider={})", endpoint, provider);

        try {
            String responseBody = httpClient.getJson(endpoint, Map.of(), timeoutSeconds);
            JsonNode root = mapper.readTree(responseBody);
            List<String> models = new ArrayList<>();

            if (root.has("models") && root.get("models").isArray()) {
                root.get("models").forEach(node -> {
                    String name = extractModelName(node);
                    if (!name.isBlank()) models.add(name);
                });
            } else if (root.has("data") && root.get("data").isArray()) {
                root.get("data").forEach(node -> {
                    String name = extractModelName(node);
                    if (!name.isBlank()) models.add(name);
                });
            }

            log.info("Modèles détectés : {}", models);
            return models.isEmpty() ? List.of("Aucun modèle détecté") : models;

        } catch (Exception e) {
            log.error("Erreur récupération modèles depuis {}", endpoint, e);
            return List.of("Erreur : " + e.getMessage());
        }
    }

    // ══════════════════════════════════════════════════════════════
    // Utilitaires
    // ══════════════════════════════════════════════════════════════

    private String resolveModelsEndpoint(String baseUrl, String provider) {
        String p = provider != null ? provider.toLowerCase() : "";
        if (p.contains("lmstudio")) return baseUrl + "/api/v1/models";
        return baseUrl + "/models";
    }

    private String extractModelName(JsonNode node) {
        String name = node.path("display_name").asText("");
        if (name.isBlank()) name = node.path("name").asText("");
        if (name.isBlank()) name = node.path("model").asText("");
        if (name.isBlank()) name = node.path("id").asText("");
        if (name.isBlank()) name = node.path("key").asText("");
        if (name.contains("/")) name = name.substring(name.lastIndexOf('/') + 1);
        return name.trim();
    }

    private String maskKey(String key) {
        if (key == null || key.length() < 8) return key;
        return key.substring(0, 4) + "..." + key.substring(key.length() - 4);
    }
}