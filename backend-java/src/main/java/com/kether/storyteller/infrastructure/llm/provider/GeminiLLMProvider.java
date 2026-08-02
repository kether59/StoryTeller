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
public class GeminiLLMProvider implements LLMProvider {

    private final LLMHttpClient httpClient;
    private final ObjectMapper mapper;

    public GeminiLLMProvider(LLMHttpClient httpClient, ObjectMapper mapper) {
        this.httpClient = httpClient;
        this.mapper = mapper;
    }

    @Override
    public String getName() {
        return "gemini";
    }

    @Override
    public String call(String systemPrompt, String userPrompt, int maxTokens, LLMConfigModel config) throws Exception {
        String model = config.getModel() != null && !config.getModel().isBlank()
                ? config.getModel()
                : "gemini-1.5-flash";

        String url = String.format(
                "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent?key=%s",
                model, config.getApiKey());

        ObjectNode body = mapper.createObjectNode();

        // systemInstruction pour Gemini
        ObjectNode systemInstruction = body.putObject("systemInstruction");
        ArrayNode systemParts = systemInstruction.putArray("parts");
        systemParts.addObject().put("text", systemPrompt);

        ArrayNode contents = body.putArray("contents");
        ObjectNode userContent = contents.addObject().put("role", "user");
        ArrayNode userParts = userContent.putArray("parts");
        userParts.addObject().put("text", userPrompt);

        ObjectNode generationConfig = body.putObject("generationConfig");
        generationConfig.put("maxOutputTokens", maxTokens);
        generationConfig.put("temperature", config.getTemperature());

        String response = httpClient.postJson(url, Map.of(), body.toString());

        JsonNode root = mapper.readTree(response);
        return root.path("candidates").get(0)
                .path("content")
                .path("parts").get(0)
                .path("text").asText();
    }
}