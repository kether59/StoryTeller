package com.kether.storyteller.service;

import com.kether.storyteller.dto.request.Requests.AIAnalysisRequest;
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

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;

/**
 * Service d'analyse IA – équivalent Python ai.py.
 * <p>
 * Intents supportés (équivalent SuggestRequest.intent) :
 * link_characters        → analyzeCharacterLinks()
 * timeline_conflicts     → findTimelineConflicts()
 * script_consistency     → checkScriptConsistency()
 * character_behavior     → checkCharacterBehavior()
 * lore_check             → checkLore()
 * <p>
 * Utilise maintenant le LLM pour une analyse intelligente
 * plutôt que des règles simplifiées.
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

    // ══════════════════════════════════════════════════════════════
    //  Routeur principal
    // ══════════════════════════════════════════════════════════════

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

    // ══════════════════════════════════════════════════════════════
    //  1. Liens entre personnages (link_characters)
    //     Analyse IA des relations entre personnages
    // ══════════════════════════════════════════════════════════════

    public SuggestionsResult analyzeCharacterLinks(Long storyId, Long manuscriptId) {
        List<StoryCharacter> chars = characterRepo.findByStoryIdOrderByNameAsc(storyId);
        
        if (chars.isEmpty()) {
            return new SuggestionsResult(List.of());
        }

        String text = manuscriptId != null ? getManuscriptText(storyId, manuscriptId) : "";
        
        String systemPrompt = """
                Tu es un expert en analyse narrative et en relations sociales.
                Tu identifies les relations entre personnages avec précision et nuance.
                
                Réponds UNIQUEMENT en JSON valide, sans texte supplémentaire.
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
                      "description": "Explication brève de la relation",
                      "confidence": 0.8
                    }
                  ]
                }
                
                Repère les relations basées sur :
                - Noms de famille (relations familiales)
                - Écarts d'âge (pairs, mentor)
                - Rôles complémentaires (maître/apprenti)
                %s
                
                Réponds UNIQUEMENT avec le JSON.
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

            log.info("Character links analyzed — count={}", suggestions.size());
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
                // Personnage pas encore né lors de l'événement
                if (ch.getBorn() != null) {
                    LocalDate born = parseDate(ch.getBorn());
                    if (born != null && born.isAfter(eventDate)) {
                        conflicts.add(new TimelineConflict(ev.getId(), ch.getId(), 
                                ch.getName() + " n'est pas encore né(e) (" + ch.getBorn() + 
                                ") lors de l'événement (" + ev.getDate() + ")"));
                    }
                }
            }
        }

        log.info("Timeline conflicts found — count={}", conflicts.size());
        return new ConflictsResult(conflicts);
    }

    // ══════════════════════════════════════════════════════════════
    //  3. Cohérence du script (script_consistency)
    //     Analyse des mentions et de la cohérence narrative
    // ══════════════════════════════════════════════════════════════

    public ScriptConsistencyResult checkScriptConsistency(Long storyId, Long manuscriptId) {
        String text = getManuscriptText(storyId, manuscriptId);
        
        List<StoryCharacter> characters = characterRepo.findByStoryId(storyId);
        List<LoreEntry> loreEntries = loreRepo.findByStoryId(storyId);

        String systemPrompt = """
                Tu es un expert en analyse narrative.
                Tu évalues la cohérence d'un texte par rapport à la world-building et aux personnages.
                
                Réponds UNIQUEMENT en JSON valide.
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
                Analyse la cohérence de ce texte narratif.
                
                PERSONNAGES CONNUS :
                %s
                
                LORE/WORLD-BUILDING :
                %s
                
                TEXTE À ANALYSER :
                ---
                %s
                ---
                
                Identifie :
                1. Les mentions de personnages (nom mentionné + nombre de mentions)
                2. Les références au lore
                3. Les éventuelles incohérences (personnage mentionné mais pas défini, lore ignoré, etc.)
                
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
                
                Réponds UNIQUEMENT avec le JSON.
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
                            "Référence au lore identifiée"))
                    .toList();

            return new ScriptConsistencyResult(charMentions, loreMentions);
        } catch (Exception e) {
            log.error("Erreur lors de la vérification de cohérence : {}", e.getMessage());
            return new ScriptConsistencyResult(Map.of(), List.of());
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  4. Comportement des personnages (character_behavior)
    //     Analyse IA de la cohérence comportementale
    // ══════════════════════════════════════════════════════════════

    public BehaviorResult checkCharacterBehavior(Long storyId, Long manuscriptId) {
        String text = getManuscriptText(storyId, manuscriptId);
        List<StoryCharacter> chars = characterRepo.findByStoryIdOrderByNameAsc(storyId);

        if (chars.isEmpty()) {
            return new BehaviorResult(List.of());
        }

        String systemPrompt = """
                Tu es un expert en psychologie narrative et en développement de personnages.
                Tu identifies les comportements incohérents ou non-vraisemblables dans un texte.
                
                Réponds UNIQUEMENT en JSON valide.
                """;

        String charDescriptions = chars.stream()
                .map(c -> "- " + c.getName() + 
                        (c.getRole() != null ? " (Rôle: " + c.getRole() + ")" : "") +
                        (c.getPersonality() != null ? " [Personnalité: " + c.getPersonality() + "]" : ""))
                .reduce((a, b) -> a + "\n" + b)
                .orElse("");

        String userPrompt = """
                Analyse la cohérence comportementale des personnages dans ce texte.
                
                PERSONNAGES ET TRAITS :
                %s
                
                TEXTE À ANALYSER :
                ---
                %s
                ---
                
                Identifie les comportements incohérents (ex : un personnage calme qui explose soudain, 
                un pacifiste qui tue, un timide qui harangue une foule, etc.).
                
                Format JSON :
                {
                  "issues": [
                    {
                      "character_id": "Nom",
                      "personality_trait": "calme|pacifiste|timide|etc",
                      "inconsistent_action": "Action observée",
                      "context": "Extrait du texte",
                      "severity": "low|medium|high",
                      "explanation": "Pourquoi c'est incohérent"
                    }
                  ]
                }
                
                Réponds UNIQUEMENT avec le JSON.
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

            log.info("Behavior issues found — count={}", result.size());
            return new BehaviorResult(result);
        } catch (Exception e) {
            log.error("Erreur lors de l'analyse comportementale : {}", e.getMessage());
            return new BehaviorResult(List.of());
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  5. Vérification du Lore (lore_check)
    // ══════════════════════════════════════════════════════════════

    public Map<String, List<LoreMention>> checkLore(Long storyId, Long manuscriptId) {
        String text = getManuscriptText(storyId, manuscriptId);
        List<LoreEntry> loreEntries = loreRepo.findByStoryId(storyId);

        if (loreEntries.isEmpty()) {
            return Map.of("loreAnalysis", List.of());
        }

        String systemPrompt = """
                Tu es un expert en world-building et en cohérence de lore.
                Tu identifies l'utilisation et la cohérence des concepts de monde dans un texte narratif.
                
                Réponds UNIQUEMENT en JSON valide.
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
                
                ÉLÉMENTS DE LORE :
                %s
                
                TEXTE À ANALYSER :
                ---
                %s
                ---
                
                Pour chaque concept de lore :
                - Identifie s'il est mentionné
                - Compte le nombre de références
                - Repère les incohérences (utilisation contradictoire du lore)
                
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
                
                Réponds UNIQUEMENT avec le JSON.
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

            log.info("Lore analysis complete — items analyzed={}", result.size());
            return Map.of("loreAnalysis", result);
        } catch (Exception e) {
            log.error("Erreur lors de la vérification du lore : {}", e.getMessage());
            return Map.of("loreAnalysis", List.of());
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  Utilitaires
    // ══════════════════════════════════════════════════════════════

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
