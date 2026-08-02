// infrastructure/llm/provider/OllamaLLMProvider.java
package com.kether.storyteller.infrastructure.llm.provider;

import com.kether.storyteller.infrastructure.llm.LLMHttpClient;
import com.kether.storyteller.service.llm.LLMConfigModel;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Provider Ollama (local).
 *
 * FONCTIONNEMENT :
 * - Utilise l'endpoint OpenAI-compatible d'Ollama : /v1/chat/completions
 * - Construit un body au format standard OpenAI (messages array)
 * - Gère l'URL personnalisée (localhost:11434 par défaut)
 *
 * CORRECTION par rapport à l'ancien code :
 * Avant : le body était au format Ollama natif (prompt, stream, options)
 * mais l'URL était /v1/chat/completions (OpenAI). C'était incohérent.
 * Maintenant : body ET URL sont cohérents au format OpenAI.
 */
@Component
public class OllamaLLMProvider implements LLMProvider {

    private final LLMHttpClient httpClient;
    private final ObjectMapper mapper;

    public OllamaLLMProvider(LLMHttpClient httpClient, ObjectMapper mapper) {
        this.httpClient = httpClient;
        this.mapper = mapper;
    }

    @Override
    public String getName() {
        return "ollama";
    }

    @Override
    public String call(String systemPrompt, String userPrompt, int maxTokens, LLMConfigModel config) throws Exception {
        String baseUrl = config.getOllamaUrl();
        if (baseUrl == null || baseUrl.isBlank()) {
            baseUrl = "http://localhost:11434";
        }

        String url = baseUrl.endsWith("/") ? baseUrl + "v1/chat/completions"
                : baseUrl + "/v1/chat/completions";

        ObjectNode body = mapper.createObjectNode()
                .put("model", config.getModel())
                .put("max_tokens", maxTokens)
                .put("temperature", config.getTemperature());

        ArrayNode messages = body.putArray("messages");
        messages.addObject().put("role", "system").put("content", systemPrompt);
        messages.addObject().put("role", "user").put("content", userPrompt);

        String response = httpClient.postJson(url, Map.of(), body.toString());
        JsonNode root = mapper.readTree(response);
        return root.path("choices").get(0).path("message").path("content").asText();
    }
}