// infrastructure/llm/provider/LLMProvider.java
package com.kether.storyteller.infrastructure.llm.provider;

import com.kether.storyteller.domain.model.LLMConfigModel;

/**
 * Interface technique pour un provider LLM spécifique.
 *
 * DIFFÉRENCE avec LLMGenerationPort (domaine) :
 * - LLMGenerationPort = "Je veux générer du texte" (abstrait, métier)
 * - LLMProvider = "J'appelle l'API Anthropic" (concret, technique)
 *
 * FONCTIONNEMENT :
 * - Chaque implémentation connaît le format JSON spécifique de son API
 * - Toutes utilisent LLMHttpClient pour l'HTTP
 * - LLMProviderRegistry choisit le bon provider selon la config
 */
public interface LLMProvider {
    String call(String systemPrompt, String userPrompt, int maxTokens, LLMConfigModel config) throws Exception;

    default String test(LLMConfigModel config) throws Exception {
        return call("Réponds uniquement avec le mot 'OK'.", "Test", 16, config);
    }

    String getName();  // "anthropic", "openai", "ollama"...
}