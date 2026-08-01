## 🏗️ Architecture Clean Hexagonale - StoryTeller Backend

### Vue d'ensemble

```
┌────────────────────────────────────────────────────────────────────┐
│                          REST API (HTTP)                           │
│                                                                    │
│  POST /api/extraction/analyze                                      │
│  POST /api/extraction/validate-and-create                         │
│  POST /api/ai/suggest                                              │
└────────────────────┬───────────────────────────────────────────────┘
                     │
┌────────────────────▼───────────────────────────────────────────────┐
│           INTERFACE LAYER (Controllers)                            │
│  ┌─────────────────────────────┐  ┌─────────────────────────────┐ │
│  │  ExtractionController       │  │  AIController               │ │
│  │  (60 lignes)                │  │  (50 lignes)                │ │
│  └──────────────┬──────────────┘  └──────────────┬──────────────┘ │
└─────────────────┼────────────────────────────────┼─────────────────┘
                  │                                │
                  ▼                                ▼
┌────────────────────────────────────────────────────────────────────┐
│            APPLICATION LAYER (Use Cases & DTOs)                    │
│                                                                    │
│  Extraction Use Cases              AI Analysis Use Cases          │
│  ├─ ExtractCharactersUseCase        ├─ AnalyzeCharacterLinksUseCase
│  ├─ ExtractLocationsUseCase         ├─ FindTimelineConflictsUseCase
│  ├─ ExtractTimelineUseCase          ├─ CheckScriptConsistencyUseCase
│  ├─ ExtractLoreUseCase              ├─ CheckCharacterBehaviorUseCase
│  └─ ValidateAndCreateUseCase        └─ CheckLoreConsistencyUseCase
│                                                                    │
│  Application DTOs                                                 │
│  ├─ ExtractionRequest/Response                                    │
│  ├─ ValidationRequest/Result                                      │
│  └─ AIAnalysisRequest                                             │
│                                                                    │
│  🔹 Les Use Cases NE connaissent que les PORTS (pas Spring)       │
│  🔹 Les DTOs font le lien HTTP ↔ Domain                           │
└──────────────────┬────────────────────────────────────────────────┘
                   │
                   ▼
┌────────────────────────────────────────────────────────────────────┐
│           DOMAIN LAYER (Métier, 100% Pur)                          │
│                                                                    │
│  Value Objects (Immuables)          Ports (Contrats)             │
│  ├─ ExtractedCharacter               ├─ LLMPort                  │
│  ├─ ExtractedLocation                ├─ ManuscriptRepositoryPort │
│  ├─ ExtractedTimelineEvent           ├─ CharacterRepositoryPort  │
│  ├─ ExtractedLore                    ├─ LocationRepositoryPort   │
│  ├─ CharacterRelationship            ├─ TimelineRepositoryPort   │
│  └─ TimelineConflict                 ├─ LoreRepositoryPort       │
│                                      ├─ CharacterExtractionParserPort
│  ⚡ ZÉRO dépendance externe          ├─ LocationExtractionParserPort
│  ⚡ ZÉRO annotation Spring            ├─ TimelineExtractionParserPort
│  ⚡ Testable sans mock                ├─ LoreExtractionParserPort
│                                      ├─ RelationshipParserPort
│                                      └─ TimelineConflictParserPort
└──────────────────┬────────────────────────────────────────────────┘
                   │
                   ▼
┌────────────────────────────────────────────────────────────────────┐
│      INFRASTRUCTURE LAYER (Implementations concrètes)              │
│                                                                    │
│  Repository Adapters                LLM Adapter                   │
│  ├─ JpaCharacterRepositoryAdapter    └─ LLMServiceAdapter         │
│  ├─ JpaLocationRepositoryAdapter                                  │
│  ├─ JpaTimelineRepositoryAdapter     Jackson Parsers              │
│  └─ JpaLoreRepositoryAdapter         ├─ JacksonCharacterExtractionParser
│                                       ├─ JacksonLocationExtractionParser
│  🔧 Implémentent les Ports           ├─ JacksonTimelineExtractionParser
│  🔧 Gèrent JPA, Jackson, HTTP        ├─ JacksonLoreExtractionParser
│  🔧 Couplage Spring confiné ici      ├─ JacksonRelationshipParser
│                                       └─ JacksonTimelineConflictParser
│                                                                    │
│  🔹 Changer de DB/LLM/JSON = changer que l'Infrastructure         │
└────────────────────────────────────────────────────────────────────┘
```

---

### 🔄 Flux de requête : Extraction de personnages

```
1️⃣ REQUEST: POST /api/extraction/analyze
   Body: {
     "manuscriptId": 1,
     "extractTypes": ["characters", "locations"]
   }

2️⃣ INTERFACE LAYER
   ExtractionController
   └─ Valide la requête (validation Jakarta)
   └─ Injecte : ExtractCharactersUseCase, ExtractLocationsUseCase

3️⃣ APPLICATION LAYER
   ExtractCharactersUseCase.execute(manuscriptId=1)
   ├─ Récupère le manuscrit via ManuscriptRepositoryPort
   ├─ Appelle le LLM via LLMPort (interface)
   ├─ Parse la réponse via CharacterExtractionParserPort
   └─ Retourne List<ExtractedCharacter> (domaine pur)

4️⃣ DOMAIN LAYER
   ExtractedCharacter record
   ├─ Validation stricte (constructor)
   ├─ Immuable (record)
   └─ Zéro connaissance de la persistance

5️⃣ INFRASTRUCTURE LAYER (Caché derrière les ports)
   
   🔌 ManuscriptRepositoryPort.findById()
      → JpaManuscriptRepositoryAdapter
         └─ com.kether.storyteller.entity.Manuscript (JPA Entity)
            └─ Convertit en domain.model.Manuscript

   🔌 LLMPort.generate()
      → LLMServiceAdapter
         └─ LLMService.callLLM() (HTTP → Ollama)

   🔌 CharacterExtractionParserPort.parse()
      → JacksonCharacterExtractionParser
         └─ ObjectMapper.readValue() → ExtractedCharacter

6️⃣ RESPONSE: ExtractionResponse
   {
     "characters": [
       { "name": "Alice", "surname": "Dupont", "age": 25, ... }
     ],
     "locations": [],
     "timeline": [],
     "lore": []
   }
```

---

### ✅ Propriétés de cette architecture

| Aspect | Avant | Après |
|--------|-------|-------|
| **Couplage Spring** | Partout (DTOs, Entities, Services) | Interface + Infrastructure |
| **Testabilité** | Difficile (besoin MockMvc, BD) | Facile (mock les ports) |
| **Maintenabilité** | God classes (430L, 21KB) | Séparation claire |
| **Changement DB** | Modifier 10+ fichiers | Modifier 1 adaptateur |
| **Changement LLM** | Modifier 10+ fichiers | Modifier 1 adaptateur |
| **Tests unitaires domaine** | Impossible | ✅ Sans Spring |
| **Dépendances circulaires** | Raisque | Impossible (flux unidirectionnel) |

---

### 📁 Structure de fichiers

```
src/main/java/com/kether/storyteller/
│
├── domain/                          ✨ CŒUR - Pur, immuable
│   ├── model/
│   │   ├── ExtractedCharacter.java
│   │   ├── ExtractedLocation.java
│   │   ├── ExtractedTimelineEvent.java
│   │   ├── ExtractedLore.java
│   │   ├── CharacterRelationship.java
│   │   ├── TimelineConflict.java
│   │   └── Manuscript.java
│   └── port/out/
│       ├── LLMPort.java
│       ├── ManuscriptRepositoryPort.java
│       ├── CharacterRepositoryPort.java
│       ├── LocationRepositoryPort.java
│       ├── TimelineRepositoryPort.java
│       ├── LoreRepositoryPort.java
│       ├── CharacterExtractionParserPort.java
│       ├── LocationExtractionParserPort.java
│       ├── TimelineExtractionParserPort.java
│       ├── LoreExtractionParserPort.java
│       ├── RelationshipParserPort.java
│       └── TimelineConflictParserPort.java
│
├── application/                     🎬 Orchestration
│   ├── usecase/
│   │   ├── ExtractCharactersUseCase.java
│   │   ├── ExtractLocationsUseCase.java
│   │   ├── ExtractTimelineUseCase.java
│   │   ├── ExtractLoreUseCase.java
│   │   ├── ValidateAndCreateUseCase.java
│   │   ├── AnalyzeCharacterLinksUseCase.java
│   │   ├── FindTimelineConflictsUseCase.java
│   │   ├── CheckScriptConsistencyUseCase.java
│   │   ├── CheckCharacterBehaviorUseCase.java
│   │   └── CheckLoreConsistencyUseCase.java
│   └── dto/
│       ├── ExtractionRequest.java
│       ├── ExtractionResponse.java
│       ├── ValidationRequest.java
│       ├── ValidationResult.java
│       └── AIAnalysisRequest.java
│
├── interface/                       🌐 HTTP
│   └── rest/
│       ├── ExtractionController.java
│       └── AIController.java
│
├── infrastructure/                  ⚙️ Adapters concrets
│   ├── persistence/adapter/
│   │   ├── JpaCharacterRepositoryAdapter.java
│   │   ├── JpaLocationRepositoryAdapter.java
│   │   ├── JpaTimelineRepositoryAdapter.java
│   │   ├── JpaManuscriptRepositoryAdapter.java
│   │   └── JpaLoreRepositoryAdapter.java
│   └── llm/
│       ├── adapter/
│       │   └── LLMServiceAdapter.java
│       └── parser/
│           ├── JacksonCharacterExtractionParser.java
│           ├── JacksonLocationExtractionParser.java
│           ├── JacksonTimelineExtractionParser.java
│           ├── JacksonLoreExtractionParser.java
│           ├── JacksonRelationshipParser.java
│           └── JacksonTimelineConflictParser.java
│
├── service/                         ❌ LEGACY - À supprimer
│   ├── ExtractionService.java       (DEPRECATED → Use Cases)
│   └── AIService.java               (DEPRECATED → Use Cases)
│
└── entity/                          💾 JPA Entities (infrastructure)
    ├── Manuscript.java
    ├── StoryCharacter.java
    ├── StoryLocation.java
    ├── TimelineEvent.java
    └── LoreEntry.java
```

---

### 🚀 Comment ajouter une nouvelle extraction (ex: Factions)

**Avant (god class):**
```java
// Modification de 430 lignes + 21KB...
public class ExtractionService {
    public ExtractedFactions parseFactions(Map<String, Object> data) { ... }
    
    // Ajouter le parsing au switch
    private List<ExtractedFactions> parseFactions(Map...) { ... }
}
```

**Après (architecture propre):**

```java
// 1. Domaine (10 lignes)
public record ExtractedFaction(String name, String description, double confidence) { }

// 2. Port (5 lignes)
public interface FactionExtractionParserPort {
    List<ExtractedFaction> parse(String jsonResponse);
}

// 3. Use Case (60 lignes, copie/colle ExtractCharacters)
@Component
public class ExtractFactionsUseCase {
    // ... même pattern que ExtractCharactersUseCase
}

// 4. Infrastructure (50 lignes)
@Component
public class JacksonFactionExtractionParser implements FactionExtractionParserPort {
    // ... même pattern que JacksonCharacterExtractionParser
}

// 5. Controller (3 lignes)
if (types.contains("factions"))
    factions = extractFactionsUseCase.execute(manuscriptId);
```

**Résultat:** Ajout = 127 lignes, **ZÉRO modification** des fichiers existants ✅

---

### 🧪 Tests unitaires du domaine (sans Spring)

```java
// ✅ Testable SANS @SpringBootTest, SANS MockMvc, SANS BD
@Test
void extractedCharacterValidation() {
    // Domain est pur → test classique JUnit
    assertThrows(IllegalArgumentException.class, () ->
        new ExtractedCharacter("", null, "hero", 25, ...)  // name vide
    );
}

@Test
void extractCharactersUseCaseFiltersDoublons() {
    // Mock les ports
    var mockRepo = Mockito.mock(CharacterRepositoryPort.class);
    var mockLLM = Mockito.mock(LLMPort.class);
    var mockParser = Mockito.mock(CharacterExtractionParserPort.class);
    
    when(mockLLM.generate(...)).thenReturn("...");
    when(mockParser.parse(...)).thenReturn(List.of(alice, bob));
    when(mockRepo.existsByStoryIdAndName(...)).thenReturn(true, false);  // Alice existe
    
    var useCase = new ExtractCharactersUseCase(...);
    var result = useCase.execute(1L);
    
    assertThat(result).hasSize(1);  // Que Bob (Alice filtrée)
    assertThat(result.get(0).name()).isEqualTo("Bob");
}
```

---

### 📚 Dépendances des couches

```
Interface → Application → Domain
           Infrastructure ↗
```

**Règles strictes:**
- ✅ Application dépend de Domain (via ports)
- ✅ Infrastructure implémente les ports Domain
- ✅ Interface appelle les Use Cases
- ❌ Domain ne dépend de RIEN
- ❌ Infrastructure ne dépend pas du reste (sauf Domain ports)
- ❌ Pas de dépendances circulaires

---

### 📝 Checklists pour chaque couche

#### Domain (Pur) ✅
- [ ] Records ou classes immuables
- [ ] Constructeur avec validation
- [ ] Zéro import Spring
- [ ] Zéro import Jackson
- [ ] Testable avec `new MonDomainObject()`

#### Application ✅
- [ ] Use Cases avec une seule responsabilité
- [ ] Orchestre via les ports
- [ ] DTOs font le lien REST ↔ Domain
- [ ] Spring annotations OK (@Component, @RequiredArgsConstructor)
- [ ] Zéro import JPA, Servlet, MockMvc

#### Interface ✅
- [ ] Contrôleurs fins (< 100 lignes)
- [ ] Validation HTTP (@Valid, @NotNull)
- [ ] Injection des Use Cases (pas services)
- [ ] Retourne les DTOs application

#### Infrastructure ✅
- [ ] Adapters implémentent les Ports
- [ ] Adaptent l'externe (JPA, Jackson, HTTP)
- [ ] Conversions domaine ↔ technologie
- [ ] Spring annotations OK (@Component, @Repository)

---

Généré avec ❤️ - Architecture Hexagonale Clean Code
