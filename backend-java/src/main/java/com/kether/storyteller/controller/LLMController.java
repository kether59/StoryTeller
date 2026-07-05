package com.kether.storyteller.controller;

import com.kether.storyteller.dto.request.Requests.*;
import com.kether.storyteller.dto.response.Responses.*;
import com.kether.storyteller.service.llm.LLMConfigService;
import com.kether.storyteller.service.llm.LLMProviders;
import com.kether.storyteller.service.llm.LLMService;
import jakarta.validation.Valid;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Contrôleur LLM
 */
@RestController
@RequestMapping("/api/llm")
public class LLMController {

    private final LLMConfigService configService;
    private final LLMService       llmService;
    private static final Logger log = LoggerFactory.getLogger(LLMService.class);

    public LLMController(LLMConfigService configService, LLMService llmService) {
        this.configService = configService;
        this.llmService    = llmService;
    }

    // ── Configuration ──────────────────────────────────────────────

    @GetMapping("/config")
    public LLMConfigResponse getConfig() {
        return configService.getConfigResponse();
    }

    @PostMapping("/config")
    public LLMSaveResponse saveConfig(@RequestBody LLMConfigRequest req) {
        return configService.saveConfig(req);
    }

    // ── Test de connexion ──────────────────────────────────────────

    @PostMapping("/test")
    public LLMTestResponse testConnection(@Valid @RequestBody LLMTestRequest req) {
        // On passe directement la requête au service qui gère le provider
        return llmService.testConnection(req);
    }

    // ── Health ─────────────────────────────────────────────────────

    @GetMapping("/health")
    public LLMHealthResponse health() {
        return configService.getHealth();
    }

    // ── Génération de contenu ──────────────────────────────────────

    @PostMapping("/generate-chapter")
    public GeneratedChapterResponse generateChapter(@Valid @RequestBody ChapterGenerationRequest req) {
        return llmService.generateChapter(req);
    }

    @PostMapping("/continue-writing")
    public ContinuationResponse continueWriting(@Valid @RequestBody ContinueWritingRequest req) {
        return llmService.continueWriting(req);
    }

    @PostMapping("/rewrite")
    public RewriteResponse rewrite(@Valid @RequestBody RewriteRequest req) {
        return llmService.rewrite(req);
    }

    @PostMapping("/suggest-next-scene")
    public SuggestionsResponse suggestNextScene(@Valid @RequestBody SuggestNextSceneRequest req) {
        return llmService.suggestNextScene(req);
    }

    @GetMapping("/local/models")
    public List<String> getLocalModels(
            @RequestParam String url,
            @RequestParam(required = false) String provider,
            @RequestParam(required = false, defaultValue = "60") int timeoutSeconds) {

        String baseUrl = url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
        String endpoint;

        String p = (provider != null ? provider.toLowerCase() : "");

        if (p.contains("lmstudio")) {
            endpoint = baseUrl + "/api/v1/models";
        } else if (p.contains("ollama") || p.contains("llama")) {
            endpoint = baseUrl + "/models";
        } else {
            endpoint = baseUrl + "/models";
        }

        log.info("Récupération modèles depuis {} (provider={})", endpoint, provider);

        try {
            String responseBody = LLMProviders.getJsonWithTimeout(endpoint, Map.of(), timeoutSeconds);
            JsonNode root = new ObjectMapper().readTree(responseBody);
            List<String> models = new ArrayList<>();

            // ====================== LM STUDIO ======================
            if (root.has("models") && root.get("models").isArray()) {
                root.get("models").forEach(node -> {
                    String name = extractModelName(node);
                    if (!name.isBlank()) models.add(name);
                });
            }
            // ====================== llama.cpp ======================
            else if (root.has("models") && root.get("models").isArray()) {
                root.get("models").forEach(node -> {
                    String name = extractModelName(node);
                    if (!name.isBlank()) models.add(name);
                });
            }
            // ====================== Fallback OpenAI-like ======================
            else if (root.has("data") && root.get("data").isArray()) {
                root.get("data").forEach(node -> {
                    String name = extractModelName(node);
                    if (!name.isBlank()) models.add(name);
                });
            }

            log.info("Modèles détectés : {}", models);
            return models.isEmpty()
                    ? List.of("Aucun modèle détecté")
                    : models;

        } catch (Exception e) {
            log.error("Erreur récupération modèles depuis {}", endpoint, e);
            return List.of("Erreur : " + e.getMessage());
        }
    }

    // Utilitaire robuste pour extraire le nom du modèle
    private String extractModelName(JsonNode node) {
        // Priorité haute
        String name = node.path("display_name").asString("");
        if (name.isBlank()) name = node.path("name").asString("");
        if (name.isBlank()) name = node.path("model").asString("");
        if (name.isBlank()) name = node.path("id").asString("");
        if (name.isBlank()) name = node.path("key").asString("");

        // Nettoyage du chemin complet (/models/xxx.gguf → xxx.gguf)
        if (name.contains("/")) {
            name = name.substring(name.lastIndexOf('/') + 1);
        }

        return name.trim();
    }
}