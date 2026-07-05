# StoryTeller Backend – Java 25 + Spring Boot 4.0.1

**Version** : 2.0.0  
**Stack** : Java 25 • Spring Boot 4.0.1 • Maven • SQLite • OpenNLP • Virtual Threads

Outil complet de gestion et d'écriture de romans avec assistance IA (LLM + Extraction automatique).

---

## Table des matières

1. [Démarrage rapide](#démarrage-rapide)
2. [Prérequis](#prérequis)
3. [Lancement en mode développement](#lancement-en-mode-développement)
4. [Compilation Maven détaillée](#compilation-maven-détaillée)
5. [Lancement avec Docker (recommandé)](#lancement-avec-docker)
6. [Configuration complète (Backend + DB + LLM)](#configuration-complète)
7. [Variables d’environnement importantes](#variables-denvironnement-importantes)
8. [Structure du projet](#structure-du-projet)
9. [Configuration Spring Boot](#configuration-spring-boot)
10. [Fonctionnement du LLM](#fonctionnement-du-llm)
11. [Ajouter un nouveau Provider LLM](#ajouter-un-nouveau-provider-llm)

---

## Prérequis

- **Java 25** (Temurin recommandé)
- **Maven 3.9+**
- **Docker + Docker Compose** (pour une installation complète)
- **Git**

---

## Démarrage rapide

### Mode Développement

```bash
# Depuis la racine du backend
./mvnw spring-boot:run
```

API disponible sur : **http://localhost:8000**

---

## Compilation Maven détaillée

```bash
# 1. Nettoyer + compiler
./mvnw clean compile

# 2. Compiler + tests
./mvnw test

# 3. Package complet (JAR exécutable)
./mvnw package -DskipTests

# Le JAR se trouve dans : target/storyteller-api-2.0.0.jar
```

**Lancement du JAR compilé :**

```bash
java -jar target/storyteller-api-2.0.0.jar
```

---

## Lancement avec Docker (Recommandé)

```bash
# Sans LLM (léger)
docker compose up -d

# Avec LLM complet (Ollama + llama.cpp)
docker compose --profile llm up -d --build
```

**URLs après démarrage :**

- Backend API → `http://localhost:8000`
- LLM (llama.cpp) → `http://localhost:8080`
- Frontend (optionnel) → `http://localhost:8080`

---

## Configuration complète (Backend + DB + LLM)

Le `docker-compose.yml` lance tout :

- **Backend Java** (Spring Boot)
- **SQLite** (volume persistant)
- **LLM** via llama.cpp ou Ollama

**Fichier `docker-compose.yml` recommandé** (déjà présent) :

```yaml
services:
  storyteller-api:
    build: .
    ports: ["8000:8000"]
    environment:
      - LLM_PROVIDER=ollama
      - OLLAMA_URL=http://llama-cpp:8080
    volumes:
      - storyteller-data:/app/data
    profiles: ["default"]

  llama-cpp:
    image: ghcr.io/ggerganov/llama.cpp:server
    ports: ["8080:8080"]
    volumes: ["./models:/models"]
    profiles: ["llm"]
```

---

## Variables d’environnement importantes

| Variable                    | Description                              | Valeur par défaut              |
|----------------------------|------------------------------------------|--------------------------------|
| `SERVER_PORT`              | Port de l’API                            | 8000                           |
| `DATABASE_URL`             | Chemin SQLite                            | `./data/storyteller.db`        |
| `LLM_PROVIDER`             | Provider LLM                             | `ollama`                       |
| `OLLAMA_URL`               | URL Ollama                               | `http://localhost:11434`       |
| `ANTHROPIC_API_KEY`        | Clé Anthropic                            | -                              |
| `OPENAI_API_KEY`           | Clé OpenAI                               | -                              |
| `LLM_CONFIG_PATH`          | Chemin du fichier de config LLM          | `/app/config/llm_config.json`  |
| `NLP_MODELS_PATH`          | Chemin des modèles OpenNLP               | `classpath:nlp/`               |

---

## Structure du Projet

```
backend/
├── src/main/java/com/kether/storyteller/
│   ├── StoryTellerApplication.java
│   ├── config/
│   ├── controller/
│   ├── dto/
│   ├── entity/
│   ├── exception/
│   ├── repository/
│   └── service/llm/
├── src/main/resources/
│   ├── application.yml
│   └── llm_config.json
├── pom.xml
├── Dockerfile
├── docker-compose.yml
├── run.sh
└── README.md
```

---

## Configuration Spring Boot (`application.yml`)

Points clés configurés :

```yaml
spring:
  datasource:
    url: jdbc:sqlite:${DATABASE_URL:./data/storyteller.db}
  jpa:
    hibernate:
      ddl-auto: update
  threads:
    virtual:
      enabled: true
storyteller:
  llm:
    config-file: ${LLM_CONFIG_PATH:llm_config.json}
    default-provider: ${LLM_PROVIDER:ollama}
```

---

## Fonctionnement du LLM

Le système est modulaire :

1. `LLMConfigService` → gère `llm_config.json`
2. `LLMService` → choisit le provider (`AnthropicProvider`, `OllamaProvider`, etc.)
3. Chaque provider implémente l’interface `LLMProvider`

---

## Ajouter un nouveau Provider LLM

### Étape 1 : Créer une nouvelle classe

```java
// src/main/java/com/kether/storyteller/service/llm/LLMProviders.java
public static class GrokProvider implements LLMProvider {
    @Override
    public String call(...) { ... }
}
```

### Étape 2 : L’enregistrer dans `AppConfig.java`

```java
@Bean
public GrokProvider grokProvider() {
    return new GrokProvider();
}
```

### Étape 3 : Mettre à jour `resolveProvider()` dans `LLMService.java`

```java
case "grok" -> grokProvider;
```

