package com.kether.storyteller.infrastructure.llm.provider;

import com.kether.storyteller.infrastructure.llm.LLMHttpClient;
import com.kether.storyteller.service.llm.LLMConfigModel;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

        // ✅ CORRIGÉ : endpoint LM Studio natif
        String url = baseUrl.endsWith("/") ? baseUrl + "api/v1/chat"
                : baseUrl + "/api/v1/chat";

        // ✅ CORRIGÉ : format du body selon ton curl
        ObjectNode body = mapper.createObjectNode()
                .put("model", config.getModel())
                .put("system_prompt", systemPrompt)
                .put("input", userPrompt);

        String response = httpClient.postJson(url, Map.of(), body.toString());
        JsonNode root = mapper.readTree(response);

        JsonNode output = root.path("output");
        if (output.isArray() && output.size() > 0) {
            return output.get(0).path("content").asText();
        }

        throw new RuntimeException("Réponse LM Studio invalide : " + response);
    }

    @Override
    public String test(LLMConfigModel config) throws Exception {
        // Test léger : on utilise le endpoint chat avec un prompt minimal
        return call("Réponds uniquement OK.", "Test", 16, config);
    }
}