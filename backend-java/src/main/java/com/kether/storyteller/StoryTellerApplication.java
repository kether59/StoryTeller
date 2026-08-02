package com.kether.storyteller;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Point d'entrée simplifié.
 *
 * SUPPRESSIONS :
 * - @EnableJpaRepositories → inutile, Spring Boot scanne automatiquement
 * - @EntityScan → inutile, les entités sont sous le package racine
 * - @EnableConfigurationProperties → gardé si tu as des @ConfigurationProperties
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class StoryTellerApplication {
    public static void main(String[] args) {
        SpringApplication.run(StoryTellerApplication.class, args);
    }
}