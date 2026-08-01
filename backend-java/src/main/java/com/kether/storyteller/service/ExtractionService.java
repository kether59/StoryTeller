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
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service d'extraction automatique â€“ Ã©quivalent Python extraction.py.
 *
 * Routes :
 *   POST /api/extraction/analyze           â†’ {@link #analyze(ExtractionRequest)}
 *   POST /api/extraction/validate-and-create â†’ {@link #validateAndCreate(ValidationRequest)}
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

    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
    //  Analyse (POST /api/extraction/analyze)
    //  Ã‰quivalent : async def analyze_manuscript(request, db)
    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•

    public ExtractionResult analyze(ExtractionRequest req) {
        Manuscript manuscript = manuscriptRepo.findById(req.manuscriptId())
                .orElseThrow(() -> ResourceNotFoundException.of("Manuscrit", req.manuscriptId()));

        String text = manuscript.getText();
        if (text == null || text.length() < 100) {
            throw new IllegalArgumentException("Le manuscrit est trop court pour Ãªtre analysÃ©");
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

    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
    //  Validation + crÃ©ation (POST /api/extraction/validate-and-create)
    //  Ã‰quivalent : async def validate_and_create(request, db)
    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•

    @Transactional
    public ValidationResult validateAndCreate(ValidationRequest req) {
        if (!req.approved()) {
            return new ValidationResult("rejected", req.itemType(), null, "Ã‰lÃ©ment rejetÃ©");
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
                        "Type d'Ã©lÃ©ment non supportÃ© : " + req.itemType());
            };
        } catch (Exception e) {
            log.error("Erreur crÃ©ation {} : {}", req.itemType(), e.getMessage());
            throw new RuntimeException("Erreur lors de la crÃ©ation : " + e.getMessage(), e);
        }
    }

    // â”€â”€ CrÃ©ations individuelles â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    private ValidationResult createCharacter(Long storyId, Map<String, Object> d) {
        String name = str(d, "name");
        if (characterRepo.findByStoryIdAndName(storyId, name).isPresent()) {
            return new ValidationResult("duplicate", "character", null,
                    "Le personnage Â« " + name + " Â» existe dÃ©jÃ ");
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
                    "Le lieu Â« " + name + " Â» existe dÃ©jÃ ");
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
                    "L'entrÃ©e lore Â« " + title + " Â» existe dÃ©jÃ ");
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

    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
    //  Construction des prompts
    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•

    private String buildExtractionSystemPrompt() {
        return """
                Tu es un assistant expert en analyse narrative.
                Tu extrais des informations structurÃ©es depuis des textes littÃ©raires.
                Tu dois identifier prÃ©cisÃ©ment les personnages, lieux, Ã©vÃ©nements chronologiques et Ã©lÃ©ments de lore.

                IMPORTANT :
                1. Tu dois rÃ©diger TOUTES les descriptions et contenus en FRANÃ‡AIS.
                2. RÃ©ponds UNIQUEMENT en JSON valide, sans texte supplÃ©mentaire.
                """;
    }

    private String buildExtractionUserPrompt(String text, List<String> types) {
        // Tronquer le texte Ã  8000 caractÃ¨res pour Ã©viter les timeouts
        String excerpt = text.length() > 8000 ? text.substring(0, 8000) : text;

        var sb = new StringBuilder();
        sb.append("Analyse ce texte et extrait les informations demandÃ©es.\n\n");
        sb.append("TEXTE Ã€ ANALYSER :\n---\n").append(excerpt).append("\n---\n\n");
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
                    **CHRONOLOGIE** : title, date, summary, sort_order (1,2,3â€¦),
                    character_names (liste), location_name, confidence
                    """);
        }
        if (types.contains("lore")) {
            sb.append("**LORE** : title, category, content, confidence\n");
        }

        sb.append("""

                IMPORTANT :
                - Tout le contenu textuel doit Ãªtre en FRANÃ‡AIS
                - Pour l'Ã¢ge, utilise UNIQUEMENT un nombre entier (ex: 25)
                - Retourne un JSON strict avec les clÃ©s : characters, locations, timeline, lore
                - RÃ©ponds UNIQUEMENT avec le JSON, rien d'autre
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
                
                RÃ©ponds UNIQUEMENT en JSON valide, sans texte supplÃ©mentaire.
                """;

        String userPrompt = """
                Analyse ce texte et identifie les relations entre personnages.

                PERSONNAGES CONNUS :
                %s

                TEXTE Ã€ ANALYSER :
                ---
                %s
                ---

                Identifie pour chaque paire de personnages :
                - Si une relation existe dans le texte
                - Le type de relation (ally, rival, family, romantic, mentor, enemy, neutral)
                - Une description brÃ¨ve du type de relation
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

                RÃ©ponds UNIQUEMENT avec le JSON.
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

    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
    //  Parsing de la rÃ©ponse JSON
    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•

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

    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
    //  Utilitaires
    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•

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

    /** Ã‰quivalent Python : parse_age â€“ extrait le premier entier d'une chaÃ®ne. */
    private static Integer parseAge(Object raw) {
        if (raw == null)          return null;
        if (raw instanceof Number) return ((Number) raw).intValue();
        Matcher matcher = DIGITS.matcher(raw.toString());
        return matcher.find() ? Integer.parseInt(matcher.group()) : null;
    }
}
