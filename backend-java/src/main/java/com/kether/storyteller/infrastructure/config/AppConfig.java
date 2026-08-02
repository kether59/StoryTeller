// infrastructure/config/AppConfig.java
package com.kether.storyteller.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * Beans d'infrastructure partagés.
 *
 * REMARQUE : Les providers LLM ne sont PLUS déclarés ici manuellement.
 * Ils sont des @Component, Spring les détecte automatiquement.
 * Le LLMProviderRegistry les injecte automatiquement via List<LLMProvider>.
 */
@Configuration
public class AppConfig {

    @Bean
    public ObjectMapper objectMapper() {
        return JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .build();
    }

    @Bean
    public RestClient restClient() {
        return RestClient.builder()
                .defaultHeader("User-Agent", "StoryTeller/2.0 (Java)")
                .build();
    }

    @Bean
    public ApplicationRunner startupRunner() {
        return args -> {
            System.out.println("=".repeat(60));
            System.out.println(" StoryTeller API 2.0 — Architecture Hexagonale");
            System.out.println(" Docs : http://localhost:8000/api");
            System.out.println("=".repeat(60));
        };
    }
}