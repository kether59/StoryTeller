package com.kether.storyteller.service;

import com.kether.storyteller.dto.request.Requests.*;
import com.kether.storyteller.dto.response.Responses.*;
import com.kether.storyteller.entity.*;
import com.kether.storyteller.exception.ResourceNotFoundException;
import com.kether.storyteller.repository.*;
import com.kether.storyteller.service.llm.LLMService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service d'extraction automatique – équivalent Python extraction.py.
 *
 * Routes :
 *   POST /api/extraction/analyze           → {@link #analyze(ExtractionRequest)}
 *   POST /api/extraction/validate-and-create → {@link #validateAndCreate(ValidationRequest)}
 */
@Service
public class ExtractionService {

    private static final Logger log = LoggerFactory.getLogger(ExtractionService.class);
    private static final Pattern DIGITS = Pattern.compile("\\d+");

    private final LLMService            llmService;
    private final ManuscriptRepository manuscriptRepo;
    private final CharacterRepository characterRepo;
    private final LocationRepository locationRepo;
    private final LoreEntryRepository loreRepo;
    private final TimelineEventRepository timelineRepo;
    private final StoryRepository storyRepo;
    private final ObjectMapper mapper;

    public ExtractionService(LLMService llmService,
                             ManuscriptRepository manuscriptRepo,
                             CharacterRepository characterRepo,
                             LocationRepository locationRepo,
                             LoreEntryRepository loreRepo,
                             TimelineEventRepository timelineRepo,
                             StoryRepository storyRepo,
                             ObjectMapper mapper) {
        this.llmService    = llmService;
        this.manuscriptRepo = manuscriptRepo;
        this.characterRepo = characterRepo;
        this.locationRepo  = locationRepo;
        this.loreRepo      = loreRepo;
        this.timelineRepo  = timelineRepo;
        this.storyRepo     = storyRepo;
        this.mapper        = mapper;
    }

    // ══════════════════════════════════════════════════════════════
    //  Analyse (POST /api/extraction/analyze)
    //  Équivalent : async def analyze_manuscript(request, db)
    // ══════════════════════════════════════════════════════════════

    public ExtractionResult analyze(ExtractionRequest req) {
        Manuscript manuscript = manuscriptRepo.findById(req.manuscriptId())
                .orElseThrow(() -> ResourceNotFoundException.of("Manuscrit", req.manuscriptId()));

        String text = manuscript.getText();
        if (text == null || text.length() < 100) {
            throw new IllegalArgumentException("Le manuscrit est trop court pour être analysé");
        }

        List<String> types = req.extractTypes() != null
                ? req.extractTypes()
                : List.of("characters", "locations", "timeline", "lore");

        String systemPrompt = buildExtractionSystemPrompt();
        String userPrompt   = buildExtractionUserPrompt(text, types);

        try {
            String raw     = llmService.callLLM(systemPrompt, userPrompt, 4000);
            String cleaned = cleanJson(raw);

            Map<String, Object> data = mapper.readValue(cleaned, new TypeReference<>() {});

            return new ExtractionResult(
                parseCharacters(data, types),
                parseLocations(data, types),
                parseTimeline(data, types),
                parseLore(data, types),
                raw
            );
        } catch (Exception e) {
            log.error("Erreur lors de l'extraction : {}", e.getMessage());
            return new ExtractionResult(List.of(), List.of(), List.of(), List.of(),
                    "Erreur : " + e.getMessage());
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  Validation + création (POST /api/extraction/validate-and-create)
    //  Équivalent : async def validate_and_create(request, db)
    // ══════════════════════════════════════════════════════════════

    @Transactional
    public ValidationResult validateAndCreate(ValidationRequest req) {
        if (!req.approved()) {
            return new ValidationResult("rejected", req.itemType(), null, "Élément rejeté");
        }

        Map<String, Object> d = req.itemData();
        Long storyId = req.storyId();

        try {
            return switch (req.itemType()) {
                case "character" -> createCharacter(storyId, d);
                case "location"  -> createLocation(storyId, d);
                case "timeline"  -> createTimeline(storyId, d);
                case "lore"      -> createLoreEntry(storyId, d);
                default -> throw new IllegalArgumentException(
                        "Type d'élément non supporté : " + req.itemType());
            };
        } catch (Exception e) {
            log.error("Erreur création {} : {}", req.itemType(), e.getMessage());
            throw new RuntimeException("Erreur lors de la création : " + e.getMessage(), e);
        }
    }

    // ── Créations individuelles ────────────────────────────────────

    private ValidationResult createCharacter(Long storyId, Map<String, Object> d) {
        String name = str(d, "name");
        if (characterRepo.findByStoryIdAndName(storyId, name).isPresent()) {
            return new ValidationResult("duplicate", "character", null,
                    "Le personnage « " + name + " » existe déjà");
        }
        Story story = storyRepo.findById(storyId)
                .orElseThrow(() -> ResourceNotFoundException.of("Histoire", storyId));

        var c = new StoryCharacter();
        c.setStory(story);
        c.setName(name);
        c.setSurname(str(d, "surname"));
        c.setRole(str(d, "role"));
        c.setAge(parseAge(d.get("age")));
        c.setPhysicalDescription(str(d, "physical_description"));
        c.setPersonality(str(d, "personality"));
        c.setMotivation(str(d, "motivation"));
        c.setNotes("Extrait automatiquement (confiance: "
                + String.format("%.2f", num(d, "confidence")) + ")");

        StoryCharacter saved = characterRepo.save(c);
        return new ValidationResult("created", "character", saved.getId(), null);
    }

    private ValidationResult createLocation(Long storyId, Map<String, Object> d) {
        String name = str(d, "name");
        if (locationRepo.findByStoryIdAndName(storyId, name).isPresent()) {
            return new ValidationResult("duplicate", "location", null,
                    "Le lieu « " + name + " » existe déjà");
        }
        Story story = storyRepo.findById(storyId)
                .orElseThrow(() -> ResourceNotFoundException.of("Histoire", storyId));

        var l = new StoryLocation();
        l.setStory(story);
        l.setName(name);
        l.setType(str(d, "type"));
        l.setSummary(str(d, "summary"));

        StoryLocation saved = locationRepo.save(l);
        return new ValidationResult("created", "location", saved.getId(), null);
    }

    private ValidationResult createTimeline(Long storyId, Map<String, Object> d) {
        Story story = storyRepo.findById(storyId)
                .orElseThrow(() -> ResourceNotFoundException.of("Histoire", storyId));

        var ev = new TimelineEvent();
        ev.setStory(story);
        ev.setTitle(str(d, "title"));
        ev.setDate(str(d, "date"));
        ev.setSummary(str(d, "summary"));
        ev.setSortOrder(d.get("sort_order") instanceof Number n ? n.intValue() : 0);

        TimelineEvent saved = timelineRepo.save(ev);
        return new ValidationResult("created", "timeline", saved.getId(), null);
    }

    private ValidationResult createLoreEntry(Long storyId, Map<String, Object> d) {
        String title = str(d, "title");
        if (loreRepo.findByStoryIdAndTitle(storyId, title).isPresent()) {
            return new ValidationResult("duplicate", "lore", null,
                    "L'entrée lore « " + title + " » existe déjà");
        }
        Story story = storyRepo.findById(storyId)
                .orElseThrow(() -> ResourceNotFoundException.of("Histoire", storyId));

        var e = new LoreEntry();
        e.setStory(story);
        e.setTitle(title);
        e.setCategory(str(d, "category"));
        e.setContent(str(d, "content"));

        LoreEntry saved = loreRepo.save(e);
        return new ValidationResult("created", "lore", saved.getId(), null);
    }

    // ══════════════════════════════════════════════════════════════
    //  Construction des prompts
    // ══════════════════════════════════════════════════════════════

    private String buildExtractionSystemPrompt() {
        return """
                Tu es un assistant expert en analyse narrative.
                Tu extrais des informations structurées depuis des textes littéraires.
                Tu dois identifier précisément les personnages, lieux, événements chronologiques et éléments de lore.

                IMPORTANT :
                1. Tu dois rédiger TOUTES les descriptions et contenus en FRANÇAIS.
                2. Réponds UNIQUEMENT en JSON valide, sans texte supplémentaire.
                """;
    }

    private String buildExtractionUserPrompt(String text, List<String> types) {
        // Tronquer le texte à 8000 caractères pour éviter les timeouts
        String excerpt = text.length() > 8000 ? text.substring(0, 8000) : text;

        var sb = new StringBuilder();
        sb.append("Analyse ce texte et extrait les informations demandées.\n\n");
        sb.append("TEXTE À ANALYSER :\n---\n").append(excerpt).append("\n---\n\n");
        sb.append("INSTRUCTIONS D'EXTRACTION :\n");

        if (types.contains("characters")) {
            sb.append("""
                    **PERSONNAGES** : name, surname, role, age (entier pur), physical_description,
                    personality, motivation, confidence (0.0-1.0)
                    """);
        }
        if (types.contains("locations")) {
            sb.append("**LIEUX** : name, type, summary, confidence\n");
        }
        if (types.contains("timeline")) {
            sb.append("""
                    **CHRONOLOGIE** : title, date, summary, sort_order (1,2,3…),
                    character_names (liste), location_name, confidence
                    """);
        }
        if (types.contains("lore")) {
            sb.append("**LORE** : title, category, content, confidence\n");
        }

        sb.append("""

                IMPORTANT :
                - Tout le contenu textuel doit être en FRANÇAIS
                - Pour l'âge, utilise UNIQUEMENT un nombre entier (ex: 25)
                - Retourne un JSON strict avec les clés : characters, locations, timeline, lore
                - Réponds UNIQUEMENT avec le JSON, rien d'autre
                """);

        return sb.toString();
    }

    public RelationshipAnalysisResult analyzeRelationships(Long manuscriptId) {
        Manuscript manuscript = manuscriptRepo.findById(manuscriptId)
                .orElseThrow(() -> ResourceNotFoundException.of("Manuscrit", manuscriptId));

        Story story = manuscript.getStory();
        String text = manuscript.getText();

        if (text == null || text.length() < 100) {
            throw new IllegalArgumentException("Le manuscrit est trop court pour analyser les relations");
        }

        List<StoryCharacter> characters = characterRepo.findByStoryId(story.getId());
        String charSummary = characters.stream()
                .map(c -> c.getName() + (c.getSurname() != null ? " " + c.getSurname() : ""))
                .reduce((a, b) -> a + ", " + b)
                .orElse("(aucun)");

        String systemPrompt = """
                Tu es un expert en analyse narrative et en relations sociales.
                Tu analyses les textes pour identifier les relations entre personnages,
                les conflits, les alliances et les dynamiques sociales.
                
                Réponds UNIQUEMENT en JSON valide, sans texte supplémentaire.
                """;

        String userPrompt = """
                Analyse ce texte et identifie les relations entre personnages.

                PERSONNAGES CONNUS :
                %s

                TEXTE À ANALYSER :
                ---
                %s
                ---

                Identifie pour chaque paire de personnages :
                - Si une relation existe dans le texte
                - Le type de relation (ally, rival, family, romantic, mentor, enemy, neutral)
                - Une description brève du type de relation
                - Un niveau de confiance (0.0-1.0)

                Format JSON requis :
                {
                  "relationships": [
                    {
                      "character_1": "Nom Complet",
                      "character_2": "Nom Complet",
                      "type": "ally|rival|family|romantic|mentor|enemy|neutral",
                      "description": "...",
                      "confidence": 0.9,
                      "evidence": "citation du texte"
                    }
                  ]
                }

                Réponds UNIQUEMENT avec le JSON.
                """.formatted(charSummary, truncateText(text, 6000));

        try {
            String raw = llmService.callLLM(systemPrompt, userPrompt, 3000);
            String cleaned = cleanJson(raw);
            Map<String, Object> data = mapper.readValue(cleaned, new TypeReference<>() {});

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> rels = (List<Map<String, Object>>) data.getOrDefault("relationships", List.of());

            List<CharacterRelationship> relationships = rels.stream().map(r -> new CharacterRelationship(
                    str(r, "character_1"),
                    str(r, "character_2"),
                    str(r, "type"),
                    str(r, "description"),
                    num(r, "confidence"),
                    str(r, "evidence")
            )).toList();

            return new RelationshipAnalysisResult(relationships, raw);
        } catch (Exception e) {
            log.error("Erreur lors de l'analyse des relations : {}", e.getMessage());
            return new RelationshipAnalysisResult(List.of(), "Erreur : " + e.getMessage());
        }
    }

    private String truncateText(String text, int maxChars) {
        return text.length() > maxChars ? text.substring(0, maxChars) + "..." : text;
    }

    // ══════════════════════════════════════════════════════════════
    //  Parsing de la réponse JSON
    // ══════════════════════════════════════════════════════════════

    @SuppressWarnings("unchecked")
    private List<ExtractedCharacter> parseCharacters(Map<String, Object> data, List<String> types) {
        if (!types.contains("characters")) return List.of();
        List<Map<String, Object>> list = (List<Map<String, Object>>) data.getOrDefault("characters", List.of());
        return list.stream().map(m -> new ExtractedCharacter(
            str(m, "name"), str(m, "surname"), str(m, "role"),
            parseAge(m.get("age")),
            str(m, "physical_description"), str(m, "personality"),
            str(m, "motivation"), num(m, "confidence")
        )).toList();
    }

    @SuppressWarnings("unchecked")
    private List<ExtractedLocation> parseLocations(Map<String, Object> data, List<String> types) {
        if (!types.contains("locations")) return List.of();
        List<Map<String, Object>> list = (List<Map<String, Object>>) data.getOrDefault("locations", List.of());
        return list.stream().map(m -> new ExtractedLocation(
            str(m, "name"), str(m, "type"), str(m, "summary"), num(m, "confidence")
        )).toList();
    }

    @SuppressWarnings("unchecked")
    private List<ExtractedTimelineEvent> parseTimeline(Map<String, Object> data, List<String> types) {
        if (!types.contains("timeline")) return List.of();
        List<Map<String, Object>> list = (List<Map<String, Object>>) data.getOrDefault("timeline", List.of());
        return list.stream().map(m -> new ExtractedTimelineEvent(
            str(m, "title"), str(m, "date"), str(m, "summary"),
            m.get("sort_order") instanceof Number n ? n.intValue() : 0,
            m.get("character_names") instanceof List<?> l
                ? l.stream().map(Object::toString).toList() : List.of(),
            str(m, "location_name"), num(m, "confidence")
        )).toList();
    }

    @SuppressWarnings("unchecked")
    private List<ExtractedLore> parseLore(Map<String, Object> data, List<String> types) {
        if (!types.contains("lore")) return List.of();
        List<Map<String, Object>> list = (List<Map<String, Object>>) data.getOrDefault("lore", List.of());
        return list.stream().map(m -> new ExtractedLore(
            str(m, "title"), str(m, "category"), str(m, "content"), num(m, "confidence")
        )).toList();
    }

    // ══════════════════════════════════════════════════════════════
    //  Utilitaires
    // ══════════════════════════════════════════════════════════════

    private static String cleanJson(String raw) {
        return raw.strip()
                .replaceAll("(?s)^```json\\s*", "")
                .replaceAll("(?s)^```\\s*",     "")
                .replaceAll("(?s)\\s*```$",     "")
                .strip();
    }

    private static String str(Map<String, Object> m, String key) {
        Object v = m.get(key);
        return v != null ? v.toString() : null;
    }

    private static double num(Map<String, Object> m, String key) {
        Object v = m.get(key);
        if (v instanceof Number n) return n.doubleValue();
        return 0.0;
    }

    /** Équivalent Python : parse_age – extrait le premier entier d'une chaîne. */
    private static Integer parseAge(Object raw) {
        if (raw == null)          return null;
        if (raw instanceof Number) return ((Number) raw).intValue();
        Matcher matcher = DIGITS.matcher(raw.toString());
        return matcher.find() ? Integer.parseInt(matcher.group()) : null;
    }
}
