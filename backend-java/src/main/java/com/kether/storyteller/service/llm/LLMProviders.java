package com.kether.storyteller.service.llm;

import com.kether.storyteller.exception.ServiceUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

/**
 * Implémentations des fournisseurs LLM.
 *
 * Équivalents Python (llm.py + llm_config.py) :
 *   call_anthropic()   → {@link AnthropicProvider#call(String, String, int, LLMConfigModel)}
 *   call_openai()      → {@link OpenAIProvider#call(String, String, int, LLMConfigModel)}
 *   call_openrouter()  → {@link OpenRouterProvider#call(String, String, int, LLMConfigModel)}
 *   call_ollama()      → {@link OllamaProvider#call(String, String, int, LLMConfigModel)}
 */
public class LLMProviders {

    private static final Logger log = LoggerFactory.getLogger(LLMProviders.class);

    // ══════════════════════════════════════════════════════════════
    //  Interface commune
    // ══════════════════════════════════════════════════════════════

    public interface LLMProvider {
        /** @return texte généré */
        String call(String systemPrompt, String userPrompt,
                    int maxTokens, LLMConfigModel config) throws Exception;

        /** Test minimal de connectivité (équivalent /api/llm/test Python). */
        default String test(LLMConfigModel config) throws Exception {
            return call("Réponds uniquement avec le mot 'OK'.", "Test", 16, config);
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  Utilitaire HTTP partagé
    // ══════════════════════════════════════════════════════════════

    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .build();

    private static final ObjectMapper JSON = new ObjectMapper();

    static String postJson(String url, Map<String, String> headers, String body) throws Exception {
        var reqBuilder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(300))
                .POST(HttpRequest.BodyPublishers.ofString(body));
        headers.forEach(reqBuilder::header);
        reqBuilder.header("Content-Type", "application/json");

        HttpResponse<String> response = HTTP.send(reqBuilder.build(),
                HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 400) {
            throw new ServiceUnavailableException(
                    "Erreur HTTP " + response.statusCode() + " : " + response.body());
        }
        return response.body();
    }

    // ══════════════════════════════════════════════════════════════
    //  Anthropic (Claude)
    // ══════════════════════════════════════════════════════════════

    public static class AnthropicProvider implements LLMProvider {

        @Override
        public String call(String systemPrompt, String userPrompt,
                           int maxTokens, LLMConfigModel config) throws Exception {
            String apiKey = config.getApiKey();
            if (apiKey == null || apiKey.isBlank()) {
                throw new ServiceUnavailableException("ANTHROPIC_API_KEY non configurée");
            }

            ObjectNode body = JSON.createObjectNode()
                    .put("model",      config.getModel())
                    .put("max_tokens", maxTokens)
                    .put("system",     systemPrompt);

            ArrayNode messages = body.putArray("messages");
            messages.addObject()
                    .put("role",    "user")
                    .put("content", userPrompt);

            String responseBody = postJson(
                    "https://api.anthropic.com/v1/messages",
                    Map.of(
                            "x-api-key",          apiKey,
                            "anthropic-version",  "2023-06-01"
                    ),
                    body.toString()
            );

            JsonNode root = JSON.readTree(responseBody);
            return root.path("content").get(0).path("text").asText();
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  OpenAI
    // ══════════════════════════════════════════════════════════════

    public static class OpenAIProvider implements LLMProvider {

        @Override
        public String call(String systemPrompt, String userPrompt,
                           int maxTokens, LLMConfigModel config) throws Exception {
            String apiKey = config.getApiKey();
            if (apiKey == null || apiKey.isBlank()) {
                throw new ServiceUnavailableException("OPENAI_API_KEY non configurée");
            }
            return callOpenAICompatible(
                    "https://api.openai.com/v1/chat/completions",
                    apiKey, null, null,
                    systemPrompt, userPrompt, maxTokens, config
            );
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  OpenRouter
    // ══════════════════════════════════════════════════════════════

    public static class OpenRouterProvider implements LLMProvider {

        @Override
        public String call(String systemPrompt, String userPrompt,
                           int maxTokens, LLMConfigModel config) throws Exception {
            String apiKey = config.getApiKey();
            if (apiKey == null || apiKey.isBlank()) {
                throw new ServiceUnavailableException("OPENROUTER_API_KEY non configurée");
            }
            if (config.getModel() == null || config.getModel().isBlank()) {
                throw new IllegalArgumentException(
                        "Veuillez renseigner un modèle OpenRouter (ex: google/gemini-2.5-pro-preview)");
            }
            return callOpenAICompatible(
                    "https://openrouter.ai/api/v1/chat/completions",
                    apiKey,
                    "https://storyteller.app",
                    "StoryTeller",
                    systemPrompt, userPrompt, maxTokens, config
            );
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  Ollama (local)
    // ══════════════════════════════════════════════════════════════

    public static class OllamaProvider implements LLMProvider {

        @Override
        public String call(String systemPrompt, String userPrompt,
                           int maxTokens, LLMConfigModel config) throws Exception {
            String ollamaUrl = config.getOllamaUrl();
            if (ollamaUrl == null || ollamaUrl.isBlank()) {
                ollamaUrl = "http://localhost:11434";
            }

            String prompt = systemPrompt + "\n\nUser: " + userPrompt + "\n\nAssistant:";
            ObjectNode body = JSON.createObjectNode()
                    .put("model",   config.getModel())
                    .put("prompt",  prompt)
                    .put("stream",  false);

            body.putObject("options")
                    .put("temperature",  config.getTemperature())
                    .put("num_predict",  maxTokens);

            String responseBody = postJson(ollamaUrl + "/api/generate",
                    Map.of(), body.toString());

            JsonNode root = JSON.readTree(responseBody);
            return root.path("response").asText("");
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  Utilitaire OpenAI-compatible (OpenAI + OpenRouter partagent le format)
    // ══════════════════════════════════════════════════════════════

    static String callOpenAICompatible(
            String url, String apiKey,
            String httpReferer, String xTitle,
            String systemPrompt, String userPrompt,
            int maxTokens, LLMConfigModel config) throws Exception {

        ObjectNode body = JSON.createObjectNode()
                .put("model",      config.getModel())
                .put("max_tokens", maxTokens)
                .put("temperature", config.getTemperature());

        ArrayNode messages = body.putArray("messages");
        messages.addObject().put("role", "system").put("content", systemPrompt);
        messages.addObject().put("role", "user").put("content", userPrompt);

        var headers = new java.util.HashMap<String, String>();
        headers.put("Authorization", "Bearer " + apiKey);
        if (httpReferer != null) headers.put("HTTP-Referer", httpReferer);
        if (xTitle      != null) headers.put("X-Title",      xTitle);

        String responseBody = postJson(url, headers, body.toString());
        JsonNode root = JSON.readTree(responseBody);
        return root.path("choices").get(0).path("message").path("content").asText();
    }
}