package com.kether.storyteller.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = "com.storyteller.repository")
public class JpaConfig {
    // Configuration JPA - les repositories seront auto-découverts
}