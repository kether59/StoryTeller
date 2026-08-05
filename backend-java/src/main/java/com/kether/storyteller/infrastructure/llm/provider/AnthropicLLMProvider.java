// infrastructure/llm/provider/AnthropicLLMProvider.java
package com.kether.storyteller.infrastructure.llm.provider;

import com.kether.storyteller.exception.ServiceUnavailableException;
import com.kether.storyteller.infrastructure.llm.LLMHttpClient;
import com.kether.storyteller.domain.model.LLMConfigModel;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Provider Anthropic (Claude).
 *
 * FONCTIONNEMENT :
 * - Construit le payload JSON spécifique à l'API Anthropic
 * - Appelle https://api.anthropic.com/v1/messages
 * - Parse la réponse pour extraire le texte généré
 *
 * AVANT : C'était une classe statique interne de LLMProviders (500 lignes).
 * Maintenant c'est un bean Spring autonome de ~40 lignes.
 */
@Component
public class AnthropicLLMProvider implements LLMProvider {

    private final LLMHttpClient httpClient;
    private final ObjectMapper mapper;

    public AnthropicLLMProvider(LLMHttpClient httpClient, ObjectMapper mapper) {
        this.httpClient = httpClient;
        this.mapper = mapper;
    }

    @Override
    public String getName() {
        return "anthropic";
    }

    @Override
    public String call(String systemPrompt, String userPrompt, int maxTokens, LLMConfigModel config) throws Exception {
        String apiKey = config.getApiKey();
        if (apiKey == null || apiKey.isBlank()) {
            throw new ServiceUnavailableException("ANTHROPIC_API_KEY non configurée");
        }

        ObjectNode body = mapper.createObjectNode()
                .put("model", config.getModel())
                .put("max_tokens", maxTokens)
                .put("system", systemPrompt);

        ArrayNode messages = body.putArray("messages");
        messages.addObject().put("role", "user").put("content", userPrompt);

        String response = httpClient.postJson(
                "https://api.anthropic.com/v1/messages",
                Map.of("x-api-key", apiKey, "anthropic-version", "2023-06-01"),
                body.toString());

        JsonNode root = mapper.readTree(response);
        return root.path("content").get(0).path("text").asText();
    }
}