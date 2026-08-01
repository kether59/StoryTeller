package com.kether.storyteller.service;

import com.kether.storyteller.dto.request.Requests.AIAnalysisRequest;
import com.kether.storyteller.dto.response.Responses.*;
import com.kether.storyteller.exception.ResourceNotFoundException;
import com.kether.storyteller.entity.LoreEntry;
import com.kether.storyteller.entity.Manuscript;
import com.kether.storyteller.entity.StoryCharacter;
import com.kether.storyteller.entity.TimelineEvent;
import com.kether.storyteller.repository.*;
import com.kether.storyteller.service.llm.LLMService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;

/**
 * Service d'analyse IA â€“ Ã©quivalent Python ai.py.
 * <p>
 * Intents supportÃ©s (Ã©quivalent SuggestRequest.intent) :
 * link_characters        â†’ analyzeCharacterLinks()
 * timeline_conflicts     â†’ findTimelineConflicts()
 * script_consistency     â†’ checkScriptConsistency()
 * character_behavior     â†’ checkCharacterBehavior()
 * lore_check             â†’ checkLore()
 * <p>
 * Utilise maintenant le LLM pour une analyse intelligente
 * plutÃ´t que des rÃ¨gles simplifiÃ©es.
 */
@Service
@Transactional(readOnly = true)
public class AIService {

    private static final Logger log = LoggerFactory.getLogger(AIService.class);

    private final CharacterRepository characterRepo;
    private final TimelineEventRepository timelineRepo;
    private final ManuscriptRepository manuscriptRepo;
    private final LoreEntryRepository loreRepo;
    private final StoryRepository storyRepo;
    private final LLMService llmService;
    private final ObjectMapper mapper;

    public AIService(CharacterRepository characterRepo, TimelineEventRepository timelineRepo,
                     ManuscriptRepository manuscriptRepo, LoreEntryRepository loreRepo,
                     StoryRepository storyRepo, LLMService llmService, ObjectMapper mapper) {
        this.characterRepo = characterRepo;
        this.timelineRepo = timelineRepo;
        this.manuscriptRepo = manuscriptRepo;
        this.loreRepo = loreRepo;
        this.storyRepo = storyRepo;
        this.llmService = llmService;
        this.mapper = mapper;
    }

    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
    //  Routeur principal
    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•

    public Object analyze(AIAnalysisRequest req, Long storyId) {
        return switch (req.intent()) {
            case "link_characters" -> analyzeCharacterLinks(storyId, req.manuscriptId());
            case "timeline_conflicts" -> findTimelineConflicts(storyId);
            case "script_consistency" -> checkScriptConsistency(storyId, req.manuscriptId());
            case "character_behavior" -> checkCharacterBehavior(storyId, req.manuscriptId());
            case "lore_check" -> checkLore(storyId, req.manuscriptId());
            default -> throw new IllegalArgumentException("Intent inconnu : " + req.intent());
        };
    }

    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
    //  1. Liens entre personnages (link_characters)
    //     Analyse IA des relations entre personnages
    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•

    public SuggestionsResult analyzeCharacterLinks(Long storyId, Long manuscriptId) {
        List<StoryCharacter> chars = characterRepo.findByStoryIdOrderByNameAsc(storyId);
        
        if (chars.isEmpty()) {
            return new SuggestionsResult(List.of());
        }

        String text = manuscriptId != null ? getManuscriptText(storyId, manuscriptId) : "";
        
        String systemPrompt = """
                Tu es un expert en analyse narrative et en relations sociales.
                Tu identifies les relations entre personnages avec prÃ©cision et nuance.
                
                RÃ©ponds UNIQUEMENT en JSON valide, sans texte supplÃ©mentaire.
                """;

        String charDescriptions = chars.stream()
                .map(c -> "- " + c.getName() + (c.getSurname() != null ? " " + c.getSurname() : "") +
                        (c.getRole() != null ? " (" + c.getRole() + ")" : ""))
                .reduce((a, b) -> a + "\n" + b)
                .orElse("");

        String userPrompt = """
                Analyse les relations potentielles entre ces personnages.
                
                PERSONNAGES :
                %s
                %s
                
                Format JSON requis :
                {
                  "suggestions": [
                    {
                      "type": "family|ally|rival|mentor|romantic|enemy|peer|neutral",
                      "character_ids": [id1, id2],
                      "description": "Explication brÃ¨ve de la relation",
                      "confidence": 0.8
                    }
                  ]
                }
                
                RepÃ¨re les relations basÃ©es sur :
                - Noms de famille (relations familiales)
                - Ã‰carts d'Ã¢ge (pairs, mentor)
                - RÃ´les complÃ©mentaires (maÃ®tre/apprenti)
                %s
                
                RÃ©ponds UNIQUEMENT avec le JSON.
                """.formatted(
                charDescriptions,
                text.isEmpty() ? "" : "\nTEXTE DU MANUSCRIT (si disponible):\n---\n" + truncateText(text, 4000) + "\n---",
                text.isEmpty() ? "" : "\nIdentifie aussi les relations explicites du texte (alliances, conflits, etc.)"
        );

        try {
            String raw = llmService.callLLM(systemPrompt, userPrompt, 2500);
            String cleaned = cleanJson(raw);
            Map<String, Object> data = mapper.readValue(cleaned, new TypeReference<>() {});

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> suggs = (List<Map<String, Object>>) data.getOrDefault("suggestions", List.of());

            List<CharacterLinkSuggestion> suggestions = suggs.stream().map(s -> {
                @SuppressWarnings("unchecked")
                List<Object> ids = (List<Object>) s.getOrDefault("character_ids", List.of());
                List<Long> charIds = ids.stream()
                        .map(id -> id instanceof Number n ? n.longValue() : null)
                        .filter(Objects::nonNull)
                        .toList();
                return new CharacterLinkSuggestion(
                        str(s, "type"),
                        charIds,
                        str(s, "description")
                );
            }).toList();

            log.info("Character links analyzed â€” count={}", suggestions.size());
            return new SuggestionsResult(suggestions);
        } catch (Exception e) {
            log.error("Erreur lors de l'analyse des liens : {}", e.getMessage());
            return new SuggestionsResult(List.of());
        }
    }

    public ConflictsResult findTimelineConflicts(Long storyId) {
        List<TimelineEvent> events = timelineRepo.findByStoryIdWithCharacters(storyId);
        List<TimelineConflict> conflicts = new ArrayList<>();

        for (TimelineEvent ev : events) {
            if (ev.getDate() == null) continue;

            LocalDate eventDate = parseDate(ev.getDate());
            if (eventDate == null) continue;

            for (StoryCharacter ch : ev.getCharacters()) {
                // Personnage pas encore nÃ© lors de l'Ã©vÃ©nement
                if (ch.getBorn() != null) {
                    LocalDate born = parseDate(ch.getBorn());
                    if (born != null && born.isAfter(eventDate)) {
                        conflicts.add(new TimelineConflict(ev.getId(), ch.getId(), 
                                ch.getName() + " n'est pas encore nÃ©(e) (" + ch.getBorn() + 
                                ") lors de l'Ã©vÃ©nement (" + ev.getDate() + ")"));
                    }
                }
            }
        }

        log.info("Timeline conflicts found â€” count={}", conflicts.size());
        return new ConflictsResult(conflicts);
    }

    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
    //  3. CohÃ©rence du script (script_consistency)
    //     Analyse des mentions et de la cohÃ©rence narrative
    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•

    public ScriptConsistencyResult checkScriptConsistency(Long storyId, Long manuscriptId) {
        String text = getManuscriptText(storyId, manuscriptId);
        
        List<StoryCharacter> characters = characterRepo.findByStoryId(storyId);
        List<LoreEntry> loreEntries = loreRepo.findByStoryId(storyId);

        String systemPrompt = """
                Tu es un expert en analyse narrative.
                Tu Ã©values la cohÃ©rence d'un texte par rapport Ã  la world-building et aux personnages.
                
                RÃ©ponds UNIQUEMENT en JSON valide.
                """;

        String charList = characters.stream()
                .map(c -> c.getName())
                .filter(n -> n != null && !n.isBlank())
                .reduce((a, b) -> a + ", " + b)
                .orElse("");

        String loreList = loreEntries.stream()
                .filter(e -> e.getTitle() != null)
                .map(e -> e.getTitle() + (e.getCategory() != null ? " (" + e.getCategory() + ")" : ""))
                .reduce((a, b) -> a + ", " + b)
                .orElse("");

        String userPrompt = """
                Analyse la cohÃ©rence de ce texte narratif.
                
                PERSONNAGES CONNUS :
                %s
                
                LORE/WORLD-BUILDING :
                %s
                
                TEXTE Ã€ ANALYSER :
                ---
                %s
                ---
                
                Identifie :
                1. Les mentions de personnages (nom mentionnÃ© + nombre de mentions)
                2. Les rÃ©fÃ©rences au lore
                3. Les Ã©ventuelles incohÃ©rences (personnage mentionnÃ© mais pas dÃ©fini, lore ignorÃ©, etc.)
                
                Format JSON :
                {
                  "character_mentions": [
                    {"name": "Nom", "count": 5, "present": true}
                  ],
                  "lore_mentions": [
                    {"title": "Concept", "mentioned": true, "references": 2}
                  ],
                  "issues": [
                    {"type": "undefined_reference|missing_mention|inconsistency", "description": "..."}
                  ]
                }
                
                RÃ©ponds UNIQUEMENT avec le JSON.
                """.formatted(charList, loreList, truncateText(text, 5000));

        try {
            String raw = llmService.callLLM(systemPrompt, userPrompt, 2500);
            String cleaned = cleanJson(raw);
            Map<String, Object> data = mapper.readValue(cleaned, new TypeReference<>() {});

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> mentions = (List<Map<String, Object>>) data.getOrDefault("character_mentions", List.of());
            Map<String, Integer> charMentions = new HashMap<>();
            mentions.forEach(m -> charMentions.put(str(m, "name"), 
                    m.get("count") instanceof Number n ? n.intValue() : 0));

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> loreMents = (List<Map<String, Object>>) data.getOrDefault("lore_mentions", List.of());
            List<LoreMention> loreMentions = loreMents.stream()
                    .map(m -> new LoreMention(null, str(m, "title"), "mention", 
                            "RÃ©fÃ©rence au lore identifiÃ©e"))
                    .toList();

            return new ScriptConsistencyResult(charMentions, loreMentions);
        } catch (Exception e) {
            log.error("Erreur lors de la vÃ©rification de cohÃ©rence : {}", e.getMessage());
            return new ScriptConsistencyResult(Map.of(), List.of());
        }
    }

    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
    //  4. Comportement des personnages (character_behavior)
    //     Analyse IA de la cohÃ©rence comportementale
    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•

    public BehaviorResult checkCharacterBehavior(Long storyId, Long manuscriptId) {
        String text = getManuscriptText(storyId, manuscriptId);
        List<StoryCharacter> chars = characterRepo.findByStoryIdOrderByNameAsc(storyId);

        if (chars.isEmpty()) {
            return new BehaviorResult(List.of());
        }

        String systemPrompt = """
                Tu es un expert en psychologie narrative et en dÃ©veloppement de personnages.
                Tu identifies les comportements incohÃ©rents ou non-vraisemblables dans un texte.
                
                RÃ©ponds UNIQUEMENT en JSON valide.
                """;

        String charDescriptions = chars.stream()
                .map(c -> "- " + c.getName() + 
                        (c.getRole() != null ? " (RÃ´le: " + c.getRole() + ")" : "") +
                        (c.getPersonality() != null ? " [PersonnalitÃ©: " + c.getPersonality() + "]" : ""))
                .reduce((a, b) -> a + "\n" + b)
                .orElse("");

        String userPrompt = """
                Analyse la cohÃ©rence comportementale des personnages dans ce texte.
                
                PERSONNAGES ET TRAITS :
                %s
                
                TEXTE Ã€ ANALYSER :
                ---
                %s
                ---
                
                Identifie les comportements incohÃ©rents (ex : un personnage calme qui explose soudain, 
                un pacifiste qui tue, un timide qui harangue une foule, etc.).
                
                Format JSON :
                {
                  "issues": [
                    {
                      "character_id": "Nom",
                      "personality_trait": "calme|pacifiste|timide|etc",
                      "inconsistent_action": "Action observÃ©e",
                      "context": "Extrait du texte",
                      "severity": "low|medium|high",
                      "explanation": "Pourquoi c'est incohÃ©rent"
                    }
                  ]
                }
                
                RÃ©ponds UNIQUEMENT avec le JSON.
                """.formatted(charDescriptions, truncateText(text, 5000));

        try {
            String raw = llmService.callLLM(systemPrompt, userPrompt, 2500);
            String cleaned = cleanJson(raw);
            Map<String, Object> data = mapper.readValue(cleaned, new TypeReference<>() {});

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> issues = (List<Map<String, Object>>) data.getOrDefault("issues", List.of());

            List<BehaviorIssue> result = issues.stream().map(i -> new BehaviorIssue(
                    null,
                    str(i, "character_id"),
                    str(i, "inconsistent_action"),
                    str(i, "personality_trait"),
                    str(i, "context"),
                    str(i, "explanation")
            )).toList();

            log.info("Behavior issues found â€” count={}", result.size());
            return new BehaviorResult(result);
        } catch (Exception e) {
            log.error("Erreur lors de l'analyse comportementale : {}", e.getMessage());
            return new BehaviorResult(List.of());
        }
    }

    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
    //  5. VÃ©rification du Lore (lore_check)
    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•

    public Map<String, List<LoreMention>> checkLore(Long storyId, Long manuscriptId) {
        String text = getManuscriptText(storyId, manuscriptId);
        List<LoreEntry> loreEntries = loreRepo.findByStoryId(storyId);

        if (loreEntries.isEmpty()) {
            return Map.of("loreAnalysis", List.of());
        }

        String systemPrompt = """
                Tu es un expert en world-building et en cohÃ©rence de lore.
                Tu identifies l'utilisation et la cohÃ©rence des concepts de monde dans un texte narratif.
                
                RÃ©ponds UNIQUEMENT en JSON valide.
                """;

        String loreDescriptions = loreEntries.stream()
                .filter(e -> e.getTitle() != null)
                .map(e -> "- " + e.getTitle() + 
                        (e.getCategory() != null ? " [" + e.getCategory() + "]" : "") +
                        (e.getContent() != null ? ": " + truncateText(e.getContent(), 200) : ""))
                .reduce((a, b) -> a + "\n" + b)
                .orElse("");

        String userPrompt = """
                Analyse l'utilisation du lore et de la world-building dans ce texte.
                
                Ã‰LÃ‰MENTS DE LORE :
                %s
                
                TEXTE Ã€ ANALYSER :
                ---
                %s
                ---
                
                Pour chaque concept de lore :
                - Identifie s'il est mentionnÃ©
                - Compte le nombre de rÃ©fÃ©rences
                - RepÃ¨re les incohÃ©rences (utilisation contradictoire du lore)
                
                Format JSON :
                {
                  "lore_analysis": [
                    {
                      "title": "Nom du concept",
                      "mentioned": true,
                      "reference_count": 3,
                      "consistency": "consistent|contradictory|misused",
                      "note": "Observations"
                    }
                  ]
                }
                
                RÃ©ponds UNIQUEMENT avec le JSON.
                """.formatted(loreDescriptions, truncateText(text, 5000));

        try {
            String raw = llmService.callLLM(systemPrompt, userPrompt, 2500);
            String cleaned = cleanJson(raw);
            Map<String, Object> data = mapper.readValue(cleaned, new TypeReference<>() {});

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> analysis = (List<Map<String, Object>>) data.getOrDefault("lore_analysis", List.of());

            List<LoreMention> result = analysis.stream().map(a -> new LoreMention(
                    null,
                    str(a, "title"),
                    str(a, "consistency"),
                    str(a, "note")
            )).toList();

            log.info("Lore analysis complete â€” items analyzed={}", result.size());
            return Map.of("loreAnalysis", result);
        } catch (Exception e) {
            log.error("Erreur lors de la vÃ©rification du lore : {}", e.getMessage());
            return Map.of("loreAnalysis", List.of());
        }
    }

    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
    //  Utilitaires
    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•

    private String getManuscriptText(Long storyId, Long manuscriptId) {
        if (manuscriptId == null) {
            return "";
        }
        try {
            Manuscript m = manuscriptRepo.findById(manuscriptId)
                    .orElseThrow(() -> ResourceNotFoundException.of("Manuscrit", manuscriptId));
            return m.getText() != null ? m.getText() : "";
        } catch (Exception e) {
            log.warn("Could not retrieve manuscript text", e);
            return "";
        }
    }

    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) return null;
        try {
            return LocalDate.parse(dateStr);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private static String cleanJson(String raw) {
        return raw.strip()
                .replaceAll("(?s)^```json\\s*", "")
                .replaceAll("(?s)^```\\s*", "")
                .replaceAll("(?s)\\s*```$", "")
                .strip();
    }

    private static String truncateText(String text, int maxChars) {
        return text.length() > maxChars ? text.substring(0, maxChars) + "..." : text;
    }

    private static String str(Map<String, Object> m, String key) {
        Object v = m.get(key);
        return v != null ? v.toString() : null;
    }
}
