package com.kether.storyteller;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Point d'entrée StoryTeller – migration Python/FastAPI → Java 25 / Spring Boot 4.1.0
 *
 * <p>Équivalents fonctionnels :
 * <ul>
 *   <li>FastAPI lifespan        → {@code ApplicationRunner} bean</li>
 *   <li>asyncio / async/await  → virtual threads (spring.threads.virtual.enabled=true)</li>
 *   <li>SQLAlchemy             → Spring Data JPA + Hibernate 7</li>
 *   <li>Pydantic               → Bean Validation (jakarta.validation)</li>
 *   <li>spaCy                  → Apache OpenNLP</li>
 *   <li>httpx                  → Java 21+ HttpClient / Spring RestClient</li>
 * </ul>
 */
@EnableJpaRepositories(basePackages = "com.kether.storyteller.repository")
@EntityScan(basePackages = "com.kether.storyteller.entity")
@SpringBootApplication
@EnableAsync
@EnableConfigurationProperties
@ConfigurationPropertiesScan
public class StoryTellerApplication {

    public static void main(String[] args) {
        SpringApplication.run(StoryTellerApplication.class, args);
    }
}