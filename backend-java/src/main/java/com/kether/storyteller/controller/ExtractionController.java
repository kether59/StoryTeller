package com.kether.storyteller.controller;

import com.kether.storyteller.dto.request.Requests.*;
import com.kether.storyteller.dto.response.Responses.*;
import com.kether.storyteller.service.ExtractionService;
import com.kether.storyteller.service.llm.LLMConfigService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Contrôleur d'extraction – équivalent extraction.py.
 *
 * Routes :
 *   POST /api/extraction/analyze            → analyze_manuscript()
 *   POST /api/extraction/validate-and-create → validate_and_create()
 *   GET  /api/extraction/health             → health_check()
 */
@RestController
@RequestMapping("/api/extraction")
public class ExtractionController {

    private final ExtractionService  extractionService;
    private final LLMConfigService   configService;

    public ExtractionController(ExtractionService extractionService,
                                LLMConfigService configService) {
        this.extractionService = extractionService;
        this.configService     = configService;
    }

    /**
     * POST /api/extraction/analyze
     * Analyse un manuscrit et extrait personnages, lieux, chronologie, lore.
     */
    @PostMapping("/analyze")
    public ExtractionResult analyze(@Valid @RequestBody ExtractionRequest req) {
        return extractionService.analyze(req);
    }

    /**
     * POST /api/extraction/analyze-relationships
     * Analyse les relations entre personnages dans un manuscrit.
     */
    @PostMapping("/analyze-relationships")
    public RelationshipAnalysisResult analyzeRelationships(@RequestParam Long manuscriptId) {
        return extractionService.analyzeRelationships(manuscriptId);
    }

    /**
     * POST /api/extraction/validate-and-create
     * Valide et crée un élément extrait en base de données.
     */
    @PostMapping("/validate-and-create")
    public ValidationResult validateAndCreate(@Valid @RequestBody ValidationRequest req) {
        return extractionService.validateAndCreate(req);
    }

    /**
     * GET /api/extraction/health
     * Vérifie que le LLM est configuré pour l'extraction.
     */
    @GetMapping("/health")
    public Map<String, Object> health() {
        var cfg = configService.getCurrent();
        return Map.of(
                "provider",   cfg.getProvider(),
                "configured", cfg.isConfigured()
        );
    }
}