package com.kether.storyteller.infrastructure.llm.provider;

import com.kether.storyteller.service.llm.LLMConfigModel;

public class GeminiLLMProvider implements LLMProvider {
    @Override
    public String call(String systemPrompt, String userPrompt, int maxTokens, LLMConfigModel config) throws Exception {
        return "";
    }

    @Override
    public String getName() {
        return "";
    }
}
