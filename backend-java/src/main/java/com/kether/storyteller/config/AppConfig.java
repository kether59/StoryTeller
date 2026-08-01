package com.kether.storyteller.config;

import com.kether.storyteller.service.llm.LLMProviders.AnthropicProvider;
import com.kether.storyteller.service.llm.LLMProviders.OpenAIProvider;
import com.kether.storyteller.service.llm.LLMProviders.OpenRouterProvider;
import com.kether.storyteller.service.llm.LLMProviders.OllamaProvider;
import com.kether.storyteller.service.llm.LLMProviders.GeminiProvider;
import com.kether.storyteller.service.llm.LLMProviders.LMStudioProvider;
import com.kether.storyteller.service.llm.LLMProviders.LlamaCPPProvider;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.datatype.jsr310.JavaTimeModule;

import java.time.Duration;
import java.net.http.HttpClient;

/**
 * Beans d'infrastructure partagés.
 */
@Configuration
public class AppConfig {

    @Bean
    public ObjectMapper objectMapper() {
        return JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .build();   // Let Spring Boot defaults handle serialization
    }

    @Bean
    public RestClient restClient() {
        return RestClient.builder()
                .defaultHeader("User-Agent", "StoryTeller/2.0 (Java)")
                .build();
    }

    @Bean
    public HttpClient httpClient() {
        return HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();
    }

    @Bean public AnthropicProvider  anthropicProvider()  { return new AnthropicProvider(); }
    @Bean public OpenAIProvider     openAIProvider()     { return new OpenAIProvider(); }
    @Bean public OpenRouterProvider openRouterProvider() { return new OpenRouterProvider(); }
    @Bean public OllamaProvider     ollamaProvider()     { return new OllamaProvider(); }
    @Bean public GeminiProvider     geminiProvider()     { return new GeminiProvider(); }
    @Bean public LMStudioProvider   lMStudioProvider()   { return new LMStudioProvider(); }
    @Bean public LlamaCPPProvider   llamaCPPProvider()   { return new LlamaCPPProvider(); }

    @Bean
    public ApplicationRunner startupRunner() {
        return args -> {
            System.out.println("=".repeat(60));
            System.out.println("  StoryTeller API 2.0 démarrée");
            System.out.println("  Docs : http://localhost:8000/api");
            System.out.println("=".repeat(60));
        };
    }
}