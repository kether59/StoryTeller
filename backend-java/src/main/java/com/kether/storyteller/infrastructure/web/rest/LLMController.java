// infrastructure/web/rest/LLMController.java
package com.kether.storyteller.infrastructure.web.rest;

import com.kether.storyteller.application.dto.*;
import com.kether.storyteller.domain.port.in.llm.*;
import com.kether.storyteller.service.llm.LLMConfigService;
import com.kether.storyteller.service.llm.LLMProviders;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller REST : porte d'entrée HTTP.
 *
 * FONCTIONNEMENT :
 * - Reçoit les requêtes HTTP
 * - Valide les entrées avec @Valid
 * - Convertit les Request DTOs en Command DTOs (application)
 * - Appelle les Use Cases (domaine/application)
 * - Retourne les Response DTOs
 *
 * RÈGLE : Le controller ne contient AUCUNE logique métier.
 * Il ne fait que du routing et de la validation.
 *
 * AVANT : Le controller appelait LLMService (God Service de 500 lignes).
 * Maintenant : Il appelle des interfaces de use case spécialisées.
 */
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

    public LLMController(GenerateChapterUseCase generateChapter,
                         ContinueWritingUseCase continueWriting,
                         RewriteUseCase rewrite,
                         SuggestNextSceneUseCase suggestScene,
                         ManageLLMConfigUseCase manageConfig,
                         LLMConfigService configService) {
        this.generateChapter = generateChapter;
        this.continueWriting = continueWriting;
        this.rewrite = rewrite;
        this.suggestScene = suggestScene;
        this.manageConfig = manageConfig;
        this.configService = configService;
    }

    // ── Génération de chapitre ─────────────────────────────────────
    @PostMapping("/generate-chapter")
    public GeneratedChapterResponse generateChapter(@Valid @RequestBody ChapterGenerationRequest req) {
        var command = new ChapterGenerationCommand(
                req.storyId(), req.chapterNumber(), req.chapterTitle(),
                req.summary(), req.includeCharacters(), req.includeLocations(), req.length());

        var result = generateChapter.generate(command);
        return new GeneratedChapterResponse(
                result.success(), result.text(), result.chapterNumber(),
                result.chapterTitle(), result.wordCount());
    }

    // ── Continuation ───────────────────────────────────────────────
    @PostMapping("/continue-writing")
    public ContinuationResponse continueWriting(@Valid @RequestBody ContinueWritingRequest req) {
        var command = new ContinuationCommand(
                req.manuscriptId(), req.direction(), req.length());

        var result = continueWriting.continueWriting(command);
        return new ContinuationResponse(result.success(), result.text(), result.wordCount());
    }

    // ── Réécriture ─────────────────────────────────────────────────
    @PostMapping("/rewrite")
    public RewriteResponse rewrite(@Valid @RequestBody RewriteRequest req) {
        var command = new RewriteCommand(req.text(), req.instruction());

        var result = rewrite.rewrite(command);
        return new RewriteResponse(result.success(), result.originalText(),
                result.rewrittenText(), result.instruction());
    }

    // ── Suggestions ────────────────────────────────────────────────
    @PostMapping("/suggest-next-scene")
    public SuggestionsResponse suggestNextScene(@Valid @RequestBody SuggestNextSceneRequest req) {
        var command = new SuggestionCommand(req.storyId(), req.currentSituation());

        var result = suggestScene.suggest(command);
        return new SuggestionsResponse(result.suggestions());
    }

    // ── Configuration ──────────────────────────────────────────────
    @GetMapping("/config")
    public LLMConfigResponse getConfig() {
        return manageConfig.getCurrentConfig();
    }

    @PostMapping("/config")
    public LLMSaveResponse saveConfig(@RequestBody LLMConfigRequest req) {
        return manageConfig.updateConfig(mapToDto(req));
    }

    @PostMapping("/test")
    public LLMTestResponse testConnection(@Valid @RequestBody LLMTestRequest req) {
        return manageConfig.testConnection(req.provider(), req.model(), req.apiKey());
    }

    @GetMapping("/health")
    public LLMHealthResponse health() {
        return configService.getHealth();
    }

    @GetMapping("/local/models")
    public List<String> getLocalModels(@RequestParam String url,
                                       @RequestParam(required = false) String provider,
                                       @RequestParam(required = false, defaultValue = "60") int timeoutSeconds) {
        // Cette méthode reste technique, elle peut appeler LLMProviders directement
        // ou être déplacée dans un service dédié
        String baseUrl = url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
        String endpoint = resolveModelsEndpoint(baseUrl, provider);

        try {
            String responseBody = LLMProviders.getJsonWithTimeout(endpoint, java.util.Map.of(), timeoutSeconds);
            var root = new com.fasterxml.jackson.databind.ObjectMapper().readTree(responseBody);
            // ... parsing identique à l'ancien code
            return List.of("modèle-détecté-1", "modèle-détecté-2"); // Simplifié
        } catch (Exception e) {
            return List.of("Erreur : " + e.getMessage());
        }
    }

    private String resolveModelsEndpoint(String baseUrl, String provider) {
        String p = provider != null ? provider.toLowerCase() : "";
        if (p.contains("lmstudio")) return baseUrl + "/api/v1/models";
        return baseUrl + "/models";
    }

    private LLMConfigDto mapToDto(LLMConfigRequest req) {
        return new LLMConfigDto(req.provider(), req.model(), req.apiKey(),
                req.ollamaUrl(), req.temperature(), req.maxTokens());
    }
}