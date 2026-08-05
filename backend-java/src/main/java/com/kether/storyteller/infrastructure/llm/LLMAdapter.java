// infrastructure/llm/LLMAdapter.java
package com.kether.storyteller.infrastructure.llm;

import com.kether.storyteller.domain.port.out.llm.LLMGenerationPort;
import com.kether.storyteller.infrastructure.llm.registry.LLMProviderRegistry;
import com.kether.storyteller.infrastructure.llm.config.LLMConfigService;
import org.springframework.stereotype.Component;

/**
 * Adapter : implémente le Port OUT du domaine pour la génération LLM.
 *
 * FONCTIONNEMENT :
 * - Implémente LLMGenerationPort (interface du domaine)
 * - Récupère la config active (provider, modèle, clé API...)
 * - Demande au LLMProviderRegistry le provider technique approprié
 * - Délègue l'appel HTTP au provider
 *
 * DIFFÉRENCE avec l'ancien LLMServiceAdapter :
 * AVANT : LLMServiceAdapter dépendait de LLMService (le service connaissait
 * l'adapter, pas l'inverse). C'était une inversion inversée.
 *
 * MAINTENANT : LLMAdapter dépend de LLMProviderRegistry (infrastructure)
 * et implémente LLMGenerationPort (domaine). L'application dépend du domaine.
 * La flèche va dans le bon sens : Application → Domaine ← Infrastructure.
 */
@Component
public class LLMAdapter implements LLMGenerationPort {

    private final LLMConfigService configService;
    private final LLMProviderRegistry registry;

    public LLMAdapter(LLMConfigService configService, LLMProviderRegistry registry) {
        this.configService = configService;
        this.registry = registry;
    }

    @Override
    public String generate(String systemPrompt, String userPrompt, int maxTokens) {
        try {
            var config = configService.getCurrent();
            var provider = registry.resolve(config.getProvider());
            return provider.call(systemPrompt, userPrompt, maxTokens, config);
        } catch (Exception e) {
            throw new RuntimeException("LLM generation failed: " + e.getMessage(), e);
        }
    }
}