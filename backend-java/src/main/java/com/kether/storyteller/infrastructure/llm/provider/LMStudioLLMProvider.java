package com.kether.storyteller.infrastructure.llm.provider;

import com.kether.storyteller.infrastructure.llm.LLMHttpClient;
import com.kether.storyteller.service.llm.LLMConfigModel;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class LMStudioLLMProvider implements LLMProvider {

    private final LLMHttpClient httpClient;
    private final ObjectMapper mapper;

    public LMStudioLLMProvider(LLMHttpClient httpClient, ObjectMapper mapper) {
        this.httpClient = httpClient;
        this.mapper = mapper;
    }

    @Override
    public String getName() {
        return "lmstudio";
    }

    @Override
    public String call(String systemPrompt, String userPrompt, int maxTokens, LLMConfigModel config) throws Exception {
        String baseUrl = config.getLlmUrl();
        if (baseUrl == null || baseUrl.isBlank()) {
            baseUrl = "http://localhost:1234";
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