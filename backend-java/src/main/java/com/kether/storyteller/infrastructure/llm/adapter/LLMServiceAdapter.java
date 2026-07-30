package com.kether.storyteller.infrastructure.llm.adapter;

import com.kether.storyteller.domain.port.out.LLMPort;
import com.kether.storyteller.service.llm.LLMService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LLMServiceAdapter implements LLMPort {

    // ← TON LLMSERVICE EXISTANT, on y touche pas
    private final LLMService existingLlmService;

    @Override
    public String generate(String systemPrompt, String userPrompt, int maxTokens) {
        try {
            return existingLlmService.callLLM(systemPrompt, userPrompt, maxTokens);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}