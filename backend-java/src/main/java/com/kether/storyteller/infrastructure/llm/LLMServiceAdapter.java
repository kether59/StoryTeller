package com.kether.storyteller.infrastructure.llm;

import com.kether.storyteller.domain.port.out.LLMPort;
import com.kether.storyteller.service.llm.LLMConfigService;
import com.kether.storyteller.service.llm.LLMProviders.LLMProvider;
import com.kether.storyteller.service.llm.LLMService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Adapter d'infrastructure: LLMService → LLMPort (domain)
 * 
 * LLMService gère :
 *   - Le routing des providers (Ollama, OpenAI, Claude, etc.)
 *   - La configuration dynamique
 *   - Les appels API aux services LLM externes
 * 
 * Cet adapter expone UNIQUEMENT l'interface LLMPort au domaine.
 * Le domaine ne sait pas que derrière il y a Ollama, OpenAI, ou autre.
 */
@Component
@RequiredArgsConstructor
public class LLMServiceAdapter implements LLMPort {

    private final LLMService llmService;

    @Override
    public String generate(String systemPrompt, String userPrompt, int maxTokens) {
        try {
            return llmService.callLLM(systemPrompt, userPrompt, maxTokens);
        } catch (Exception e) {
            throw new RuntimeException("LLM generation failed: " + e.getMessage(), e);
        }
    }
}
