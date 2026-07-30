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
 * <p>
 * Équivalents Python (llm.py + llm_config.py) :
 * call_anthropic()   → {@link AnthropicProvider#call(String, String, int, LLMConfigModel)}
 * call_openai()      → {@link OpenAIProvider#call(String, String, int, LLMConfigModel)}
 * call_openrouter()  → {@link OpenRouterProvider#call(String, String, int, LLMConfigModel)}
 * call_ollama()      → {@link OllamaProvider#call(String, String, int, LLMConfigModel)}
 * call_gemini()      → {@link GeminiProvider#call(String, String, int, LLMConfigModel)}²
 * call_lmstudio()    → {@link LMStudioProvider#call(String, String, int, LLMConfigModel)}²
 * call_llamacpp()    → {@link LlamaCPPProvider#call(String, String, int, LLMConfigModel)}²
 */
public class LLMProviders {

    private static final Logger log = LoggerFactory.getLogger(LLMProviders.class);

    // ══════════════════════════════════════════════════════════════
    //  Interface commune
    // ══════════════════════════════════════════════════════════════
    private static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).followRedirects(HttpClient.Redirect.NORMAL).build();

    // ══════════════════════════════════════════════════════════════
    //  Utilitaire HTTP partagé
    // ══════════════════════════════════════════════════════════════
    /**
     * HttpClient configured for local services (no redirect, simpler handshake).
     * Used specifically for LM Studio and Ollama where CORS might be an issue.
     */
    private static final HttpClient HTTP_LOCAL = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).version(HttpClient.Version.HTTP_1_1).followRedirects(HttpClient.Redirect.NEVER).build();
    private static final ObjectMapper JSON = new ObjectMapper();

    public static String postJson(String url, Map<String, String> headers, String body) throws Exception {
        log.debug("POST JSON to {} (headers={})", url, headers);
        var reqBuilder = HttpRequest.newBuilder().uri(URI.create(url)).timeout(Duration.ofSeconds(300)).POST(HttpRequest.BodyPublishers.ofString(body));

        // Add standard headers to work with local services
        reqBuilder.header("User-Agent", "StoryTeller/1.0");
        reqBuilder.header("Content-Type", "application/json");

        headers.forEach(reqBuilder::header);

        try {
            // Use HTTP_LOCAL for local services, HTTP for remote
            HttpClient client = url.contains("localhost") || url.contains("127.0.0.1") ? HTTP_LOCAL : HTTP;
            HttpResponse<String> response = client.send(reqBuilder.build(), HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 400) {
                throw new ServiceUnavailableException("Erreur HTTP " + response.statusCode() + " : " + response.body());
            }
            return response.body();
        } catch (Exception e) {
            log.error("HTTP POST failed for {}", url, e);
            throw e;
        }
    }

    /**
     * Simple GET helper for retrieving JSON from a URL (used for listing local models).
     * Models listing can take longer on slower systems, so use extended timeout.
     */
    public static String getJson(String url, Map<String, String> headers) throws Exception {
        return getJsonWithTimeout(url, headers, 60);
    }

    /**
     * Simple GET helper with configurable timeout.
     * Uses HTTP_LOCAL client for local services to avoid CORS/preflight issues.
     */
    public static String getJsonWithTimeout(String url, Map<String, String> headers, int timeoutSeconds) throws Exception {
        log.debug("GET JSON from {} (headers={}, timeout={}s)", url, headers, timeoutSeconds);
        var reqBuilder = HttpRequest.newBuilder().uri(URI.create(url)).timeout(Duration.ofSeconds(Math.max(10, timeoutSeconds)))  // Minimum 10s
                .GET();

        // Add standard headers to work with local services
        reqBuilder.header("User-Agent", "StoryTeller/1.0");
        reqBuilder.header("Accept", "application/json");

        headers.forEach(reqBuilder::header);

        try {
            HttpResponse<String> response = HTTP_LOCAL.send(reqBuilder.build(), HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 400) {
                throw new ServiceUnavailableException("Erreur HTTP " + response.statusCode() + " : " + response.body());
            }
            return response.body();
        } catch (Exception e) {
            log.error("HTTP GET failed for {}", url, e);
            throw e;
        }
    }

    static String callOpenAICompatible(String url, String apiKey, String httpReferer, String xTitle, String systemPrompt, String userPrompt, int maxTokens, LLMConfigModel config) throws Exception {

        ObjectNode body = JSON.createObjectNode().put("model", config.getModel()).put("max_tokens", maxTokens).put("temperature", config.getTemperature());

        ArrayNode messages = body.putArray("messages");
        messages.addObject().put("role", "system").put("content", systemPrompt);
        messages.addObject().put("role", "user").put("content", userPrompt);

        var headers = new java.util.HashMap<String, String>();
        headers.put("Authorization", "Bearer " + apiKey);
        if (httpReferer != null) headers.put("HTTP-Referer", httpReferer);
        if (xTitle != null) headers.put("X-Title", xTitle);

        String responseBody = postJson(url, headers, body.toString());
        JsonNode root = JSON.readTree(responseBody);
        return root.path("choices").get(0).path("message").path("content").asText();
    }

    // ══════════════════════════════════════════════════════════════
    //  Anthropic (Claude)
    // ══════════════════════════════════════════════════════════════

    public interface LLMProvider {
        /**
         * @return texte généré
         */
        String call(String systemPrompt, String userPrompt, int maxTokens, LLMConfigModel config) throws Exception;

        /**
         * Test minimal de connectivité (équivalent /api/llm/test Python).
         */
        default String test(LLMConfigModel config) throws Exception {
            return call("Réponds uniquement avec le mot 'OK'.", "Test", 16, config);
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  OpenAI
    // ══════════════════════════════════════════════════════════════

    public static class AnthropicProvider implements LLMProvider {

        @Override
        public String call(String systemPrompt, String userPrompt, int maxTokens, LLMConfigModel config) throws Exception {
            String apiKey = config.getApiKey();
            if (apiKey == null || apiKey.isBlank()) {
                throw new ServiceUnavailableException("ANTHROPIC_API_KEY non configurée");
            }

            ObjectNode body = JSON.createObjectNode().put("model", config.getModel()).put("max_tokens", maxTokens).put("system", systemPrompt);

            ArrayNode messages = body.putArray("messages");
            messages.addObject().put("role", "user").put("content", userPrompt);

            String responseBody = postJson("https://api.anthropic.com/v1/messages", Map.of("x-api-key", apiKey, "anthropic-version", "2023-06-01"), body.toString());

            JsonNode root = JSON.readTree(responseBody);
            return root.path("content").get(0).path("text").asText();
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  OpenRouter
    // ══════════════════════════════════════════════════════════════

    public static class OpenAIProvider implements LLMProvider {

        @Override
        public String call(String systemPrompt, String userPrompt, int maxTokens, LLMConfigModel config) throws Exception {
            String apiKey = config.getApiKey();
            if (apiKey == null || apiKey.isBlank()) {
                throw new ServiceUnavailableException("OPENAI_API_KEY non configurée");
            }
            return callOpenAICompatible("https://api.openai.com/v1/chat/completions", apiKey, null, null, systemPrompt, userPrompt, maxTokens, config);
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  Ollama (local)
    // ══════════════════════════════════════════════════════════════

    public static class OpenRouterProvider implements LLMProvider {

        @Override
        public String call(String systemPrompt, String userPrompt, int maxTokens, LLMConfigModel config) throws Exception {
            String apiKey = config.getApiKey();
            if (apiKey == null || apiKey.isBlank()) {
                throw new ServiceUnavailableException("OPENROUTER_API_KEY non configurée");
            }
            if (config.getModel() == null || config.getModel().isBlank()) {
                throw new IllegalArgumentException("Veuillez renseigner un modèle OpenRouter (ex: google/gemini-2.5-pro-preview)");
            }
            return callOpenAICompatible("https://openrouter.ai/api/v1/chat/completions", apiKey, "https://storyteller.app", "StoryTeller", systemPrompt, userPrompt, maxTokens, config);
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  Utilitaire OpenAI-compatible (OpenAI + OpenRouter partagent le format)
    // ══════════════════════════════════════════════════════════════

    public static class OllamaProvider implements LLMProvider {

        @Override
        public String call(String systemPrompt, String userPrompt, int maxTokens, LLMConfigModel config) throws Exception {
            String ollamaUrl = config.getOllamaUrl();
            if (ollamaUrl == null || ollamaUrl.isBlank()) {
                ollamaUrl = "http://localhost:11434";
            }

            String prompt = systemPrompt + "\n\nUser: " + userPrompt + "\n\nAssistant:";
            ObjectNode body = JSON.createObjectNode().put("model", config.getModel()).put("prompt", prompt).put("stream", false);

            body.putObject("options").put("temperature", config.getTemperature()).put("num_predict", maxTokens);

            String responseBody = postJson(ollamaUrl + "/v1/chat/completions",  // ← OpenAI-compatible
                    Map.of(), body.toString());

            JsonNode root = JSON.readTree(responseBody);
            return root.path("choices").get(0).path("message").path("content").asText("");
        }
    }

    // ==================== GEMINI (Google) ====================
    public static class GeminiProvider implements LLMProvider {

        @Override
        public String call(String systemPrompt, String userPrompt, int maxTokens, LLMConfigModel config) throws Exception {
            String apiKey = config.getGeminiApiKey();
            if (apiKey == null || apiKey.isBlank()) {
                throw new ServiceUnavailableException("GEMINI_API_KEY non configurée");
            }

            String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-pro:generateContent?key=" + apiKey;

            String body = """
                    {
                      "contents": [{
                        "parts": [
                          {"text": "%s"},
                          {"text": "%s"}
                        ]
                      }],
                      "generationConfig": {
                        "temperature": %f,
                        "maxOutputTokens": %d
                      }
                    }
                    """.formatted(systemPrompt, userPrompt, config.getTemperature(), maxTokens);

            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(body)).build();

            HttpResponse<String> response = HTTP.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 400) {
                throw new ServiceUnavailableException("Gemini Error: " + response.body());
            }

            JsonNode root = JSON.readTree(response.body());
            return root.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();
        }
    }

    // ==================== LM STUDIO ====================
    public static class LMStudioProvider implements LLMProvider {

        @Override
        public String call(String systemPrompt, String userPrompt, int maxTokens, LLMConfigModel config) throws Exception {
            String baseUrl = config.getLmstudioUrl() != null && !config.getLmstudioUrl().isBlank() ? config.getLmstudioUrl() : "http://localhost:1234";

            // Nettoyage de l'URL (évite les doubles slashes)
            if (!baseUrl.endsWith("/")) baseUrl += "/";
            String url = baseUrl + "v1/chat/completions";

            String body = """
                    {
                      "model": "local-model",
                      "messages": [
                        {"role": "system", "content": "%s"},
                        {"role": "user", "content": "%s"}
                      ],
                      "temperature": %f,
                      "max_tokens": %d
                    }
                    """.formatted(JsonEscape.escape(systemPrompt), JsonEscape.escape(userPrompt), config.getTemperature(), maxTokens);

            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(body)).build();

            HttpResponse<String> response = HTTP.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 400) {
                throw new ServiceUnavailableException("LM Studio Error " + response.statusCode() + ": " + response.body());
            }

            JsonNode root = JSON.readTree(response.body());

            // Parsing robuste (gère les MissingNode)
            JsonNode contentNode = root.path("choices").path(0).path("message").path("content");

            if (contentNode.isMissingNode() || contentNode.isNull()) {
                throw new ServiceUnavailableException("Réponse LM Studio invalide : pas de contenu généré");
            }

            return contentNode.asText();
        }
    }

    private static class JsonEscape {
        public static String escape(String s) {
            if (s == null) return "";
            return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
        }
    }

    // ==================== LLAMA CPP ====================
    public static class LlamaCPPProvider implements LLMProvider {

        @Override
        public String call(String systemPrompt, String userPrompt, int maxTokens, LLMConfigModel config) throws Exception {
            String baseUrl = config.getOllamaUrl() != null && !config.getOllamaUrl().isBlank() ? config.getOllamaUrl() : "http://llama-cpp:8080";

            if (baseUrl.endsWith("/")) {
                baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
            }

            String url = baseUrl + "/v1/chat/completions";

            ObjectNode body = JSON.createObjectNode().put("model", config.getModel()).put("max_tokens", maxTokens).put("temperature", config.getTemperature());

            ArrayNode messages = body.putArray("messages");
            messages.addObject().put("role", "system").put("content", systemPrompt);
            messages.addObject().put("role", "user").put("content", userPrompt);

            String responseBody = postJson(url, Map.of(), body.toString());
            JsonNode root = JSON.readTree(responseBody);
            return root.path("choices").get(0).path("message").path("content").asText();
        }
    }
}