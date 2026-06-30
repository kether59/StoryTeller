package com.kether.storyteller.controller;

import com.kether.storyteller.dto.request.Requests.*;
import com.kether.storyteller.dto.response.Responses.*;
import com.kether.storyteller.service.llm.LLMConfigService;
import com.kether.storyteller.service.llm.LLMService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * Contrôleur LLM – fusionne les routes de llm.py et llm_config.py Python.
 *
 * Routes :
 *   GET  /api/llm/config            → read_config()
 *   POST /api/llm/config            → save_config()
 *   POST /api/llm/test              → test_connection()
 *   GET  /api/llm/health            → health()
 *   POST /api/llm/generate-chapter  → generate_chapter()
 *   POST /api/llm/continue-writing  → continue_writing()
 *   POST /api/llm/rewrite           → rewrite_text()
 *   POST /api/llm/suggest-next-scene → suggest_next_scene()
 */
@RestController
@RequestMapping("/api/llm")
public class LLMController {

    private final LLMConfigService configService;
    private final LLMService       llmService;

    public LLMController(LLMConfigService configService, LLMService llmService) {
        this.configService = configService;
        this.llmService    = llmService;
    }

    // ── Configuration ──────────────────────────────────────────────

    /** GET /api/llm/config – clé API masquée dans la réponse */
    @GetMapping("/config")
    public LLMConfigResponse getConfig() {
        return configService.getConfigResponse();
    }

    /** POST /api/llm/config – sauvegarde et met à jour le cache */
    @PostMapping("/config")
    public LLMSaveResponse saveConfig(@RequestBody LLMConfigRequest req) {
        return configService.saveConfig(req);
    }

    /** POST /api/llm/test – test de connexion au LLM choisi */
    @PostMapping("/test")
    public LLMTestResponse testConnection(@Valid @RequestBody LLMTestRequest req) {
        return llmService.testConnection(req);
    }

    /** GET /api/llm/health – statut rapide (utilisé par WritingAssistantPanel) */
    @GetMapping("/health")
    public LLMHealthResponse health() {
        return configService.getHealth();
    }

    // ── Génération de contenu ──────────────────────────────────────

    /** POST /api/llm/generate-chapter */
    @PostMapping("/generate-chapter")
    public GeneratedChapterResponse generateChapter(
            @Valid @RequestBody ChapterGenerationRequest req) {
        return llmService.generateChapter(req);
    }

    /** POST /api/llm/continue-writing */
    @PostMapping("/continue-writing")
    public ContinuationResponse continueWriting(
            @Valid @RequestBody ContinueWritingRequest req) {
        return llmService.continueWriting(req);
    }

    /** POST /api/llm/rewrite */
    @PostMapping("/rewrite")
    public RewriteResponse rewrite(@Valid @RequestBody RewriteRequest req) {
        return llmService.rewrite(req);
    }

    /** POST /api/llm/suggest-next-scene */
    @PostMapping("/suggest-next-scene")
    public SuggestionsResponse suggestNextScene(
            @Valid @RequestBody SuggestNextSceneRequest req) {
        return llmService.suggestNextScene(req);
    }
}