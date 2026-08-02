package com.kether.storyteller.infrastructure.llm.provider;

import com.kether.storyteller.infrastructure.llm.LLMHttpClient;
import com.kether.storyteller.service.llm.LLMConfigModel;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class LlamaCPPProvider implements LLMProvider {

    private final LLMHttpClient httpClient;
    private final ObjectMapper mapper;

    public LlamaCPPProvider(LLMHttpClient httpClient, ObjectMapper mapper) {
        this.httpClient = httpClient;
        this.mapper = mapper;
    }

    @Override
    public String getName() {
        return "llama";
    }

    @Override
    public String call(String systemPrompt, String userPrompt, int maxTokens, LLMConfigModel config) throws Exception {
        String baseUrl = config.getLlmUrl();
        if (baseUrl == null || baseUrl.isBlank()) {
            baseUrl = "http://localhost:8080";
        }

        String url = baseUrl.endsWith("/") ? baseUrl + "completion"
                : baseUrl + "/completion";

        // Format llama.cpp natif
        String fullPrompt = systemPrompt + "\n\n" + userPrompt;

        ObjectNode body = mapper.createObjectNode()
                .put("prompt", fullPrompt)
                .put("n_predict", maxTokens)
                .put("temperature", config.getTemperature())
                .put("stop", "\n\n");

        String response = httpClient.postJson(url, Map.of(), body.toString());

        JsonNode root = mapper.readTree(response);
        return root.path("content").asText();
    }
}