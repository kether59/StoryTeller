package com.kether.storyteller.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Équivalent du CORSMiddleware FastAPI :
 * {@code app.add_middleware(CORSMiddleware, allow_origins=["*"], ...)}
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${storyteller.cors.allowed-origins:*}")
    private String allowedOrigins;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        String[] origins = allowedOrigins.equals("*")
                ? new String[]{"*"}
                : allowedOrigins.split(",");

        registry.addMapping("/api/**")
                .allowedOrigins(origins)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(!allowedOrigins.equals("*"))
                .maxAge(3600);
    }
}