package com.kether.storyteller.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = "com.kether.storyteller.infrastructure.persistence.jpa")
public class JpaConfig {
    // Spring Boot auto-détecte déjà, mais on force le bon package
}