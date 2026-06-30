**Here is the full English translation in clean Markdown:**

---

# StoryTeller Java Backend Module – Architecture & Technology

## Architecture Overview

The **StoryTeller** module is a Java 25 backend application built with **Spring Boot 4.1.0**. It represents a complete migration from a previous Python/FastAPI system to Java. The architecture follows modern Spring Boot principles with a well-defined modular structure.

## Detailed Technology Stack

| Component              | Technology                          | Python Equivalent (Legacy) |
|------------------------|-------------------------------------|----------------------------|
| **Runtime**            | OpenJDK 25                          | -                          |
| **Web Framework**      | Spring Boot 4.1.0                   | FastAPI                    |
| **Persistence**        | Hibernate 7 + Spring Data JPA       | SQLAlchemy                 |
| **Validation**         | Bean Validation (jakarta.validation)| Pydantic                   |
| **NLP/Text**           | Apache OpenNLP                      | spaCy                      |
| **Async Threads**      | Virtual Threads (Java 21+)          | asyncio                    |
| **HTTP Client**        | HttpClient / Spring RestClient      | httpx                      |
| **Markdown**           | react-markdown, markdown-it         | -                          |
| **Package Management** | Maven                               | pip                        |

## Source Code Structure

The application follows standard Spring Boot conventions:

```bash
com.kether.storyteller/
├── StoryTellerApplication.java          # Main entry point
├── entity/                              # JPA Entities (SQLAlchemy models)
├── repository/                          # JPA Repositories
├── controller/                          # REST Controllers
├── service/                             # Business Logic
└── configuration/                       # Configuration & Beans
```

## Main Entry Point Analysis (`StoryTellerApplication.java`)

### Key Annotations and Their Roles

| Annotation                    | Purpose                                      | FastAPI Equivalent          |
|-------------------------------|----------------------------------------------|-----------------------------|
| `@EnableJpaRepositories`      | Auto-discovers JPA repositories              | -                           |
| `@EntityScan`                 | Scans JPA entities                           | SQLAlchemy metadata         |
| `@SpringBootApplication`      | Bootstraps the entire Spring Boot app        | FastAPI()                   |
| `@EnableAsync`                | Enables asynchronous processing              | asyncio                     |
| `@EnableConfigurationProperties` | Enables configurable properties            | -                           |
| `@ConfigurationPropertiesScan` | Scans `@ConfigurationProperties` classes   | -                           |


## Application Flow

### Startup Sequence
1. `SpringApplication.run()` bootstraps the app
2. Component scanning (entities, repositories, services)
3. Property loading (`application.properties` / YAML)
4. Database initialization (Hibernate)
5. Virtual threads activation for async tasks

### HTTP Request Lifecycle
```
Client → DispatcherServlet → Controller → Service → Repository → JPA/Hibernate → Database
```

---

## How to Extend the Code

### 1. Add a New Entity

```java
package com.kether.storyteller.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "story_content")
public class StoryContent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String title;

    @Lob
    private String content;
}
```

### 2. Create a Repository

```java
@Repository
public interface StoryContentRepository extends JpaRepository<StoryContent, Long> {
    List<StoryContent> findByTitleContaining(String term);
    boolean existsByTitle(String title);
}
```

### 3. Implement a Service

```java
@Service
@Transactional
public class StoryContentService {
    private final StoryContentRepository repository;

    public StoryContentService(StoryContentRepository repository) {
        this.repository = repository;
    }

    @Async
    public void processStory(String content) {
        // Async processing with virtual threads
    }
}
```

### 4. Create a REST Controller

```java
@RestController
@RequestMapping("/api/stories")
public class StoryController {
    private final StoryContentService service;

    public StoryController(StoryContentService service) {
        this.service = service;
    }

    @GetMapping
    public List<StoryContent> getAll() {
        return service.getAll();
    }
}
```

---

## Best Practices

**✅ Do's**
- Use standard JPA annotations
- Prefer constructor injection
- Use `@Transactional` for services
- Enable proper logging (`DEBUG` level)
- Leverage Virtual Threads for I/O

**❌ Don'ts**
- Don't use `final` on JPA entity constructors
- Avoid N+1 query problems
- Never expose raw entities in production without DTOs


---

## Start the stack

Bash# Start with LLM

```bash
docker compose --profile llm up -d --build
```

Or without LLM first (for testing)

```bash
docker compose up -d --build
```

Access

- Backend API: http://localhost:8000
- Swagger: http://localhost:8000/swagger-ui.html
- LLM (llama.cpp): http://localhost:8080