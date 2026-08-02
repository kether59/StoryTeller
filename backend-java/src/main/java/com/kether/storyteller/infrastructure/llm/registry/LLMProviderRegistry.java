// infrastructure/llm/registry/LLMProviderRegistry.java
package com.kether.storyteller.infrastructure.llm.registry;

import com.kether.storyteller.infrastructure.llm.provider.LLMProvider;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Registre qui maintient une Map de tous les providers disponibles.
 *
 * FONCTIONNEMENT :
 * - Spring injecte automatiquement TOUS les beans implémentant LLMProvider
 * - On les stocke dans une Map <nom, provider>
 * - Résolution par nom : get("anthropic") → AnthropicLLMProvider
 *
 * AVANT : C'était un switch manuel dans LLMService avec 7 cases.
 * Maintenant : Ajouter un provider = créer une classe + @Component.
 * Plus besoin de toucher le switch, ni AppConfig, ni quoi que ce soit d'autre.
 * C'est l'Open/Closed Principle : ouvert à l'extension, fermé à la modification.
 */
@Component
public class LLMProviderRegistry {

    private final Map<String, LLMProvider> providers;

    public LLMProviderRegistry(List<LLMProvider> providerList) {
        this.providers = providerList.stream()
                .collect(Collectors.toMap(LLMProvider::getName, p -> p));
    }

    public LLMProvider resolve(String name) {
        if (name == null) name = "";
        LLMProvider provider = providers.get(name.toLowerCase());
        if (provider == null) {
            throw new IllegalArgumentException("Provider inconnu : " + name);
        }
        return provider;
    }

    public List<String> availableProviders() {
        return List.copyOf(providers.keySet());
    }
}