package com.kether.storyteller.service;

import com.kether.storyteller.dto.request.Requests.AIAnalysisRequest;
import com.kether.storyteller.dto.response.Responses.*;
import com.kether.storyteller.entity.*;
import com.kether.storyteller.exception.ResourceNotFoundException;
import com.kether.storyteller.repository.*;
import com.kether.storyteller.service.llm.NLPService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;

/**
 * Service d'analyse IA – équivalent Python ai.py.
 * <p>
 * Intents supportés (équivalent SuggestRequest.intent) :
 * link_characters        → findCharacterLinks()
 * timeline_conflicts     → findTimelineConflicts()
 * script_consistency     → checkScriptConsistency()
 * character_behavior     → checkCharacterBehavior()
 * lore_check             → checkLore()
 * <p>
 * Remplace spaCy par :
 * - NLPService  (mentions, entités nommées via OpenNLP)
 * - String matching simple pour les règles de comportement
 */
@Service
@Transactional(readOnly = true)
public class AIService {

    /**
     * Règles de comportement simplifiées (sans parsing syntaxique profond).
     * Pour un matching de qualité supérieure, NLPService.process() peut être
     * utilisé pour trouver les sujets des verbes via OpenNLP.
     */
    private static final Map<String, List<String>> BEHAVIOR_RULES = Map.of("calme", List.of("crier", "hurler", "exploser", "frapper"), "pacifiste", List.of("tuer", "frapper", "attaquer", "combattre"), "timide", List.of("haranguer", "commander", "exiger"));
    private final CharacterRepository characterRepo;
    private final TimelineEventRepository timelineRepo;
    private final ManuscriptRepository manuscriptRepo;
    private final LoreEntryRepository loreRepo;
    private final NLPService nlpService;

    // ══════════════════════════════════════════════════════════════
    //  Routeur principal (équivalent @router.post("/suggest"))
    // ══════════════════════════════════════════════════════════════

    public AIService(CharacterRepository characterRepo, TimelineEventRepository timelineRepo, ManuscriptRepository manuscriptRepo, LoreEntryRepository loreRepo, NLPService nlpService) {
        this.characterRepo = characterRepo;
        this.timelineRepo = timelineRepo;
        this.manuscriptRepo = manuscriptRepo;
        this.loreRepo = loreRepo;
        this.nlpService = nlpService;
    }

    // ══════════════════════════════════════════════════════════════
    //  1. Liens entre personnages (link_characters)
    //     Équivalent Python : if request.intent == "link_characters"
    // ══════════════════════════════════════════════════════════════

    public Object analyze(AIAnalysisRequest req, Long storyId) {
        return switch (req.intent()) {
            case "link_characters" -> findCharacterLinks(storyId);
            case "timeline_conflicts" -> findTimelineConflicts(storyId);
            case "script_consistency" -> checkScriptConsistency(storyId, req.manuscriptId());
            case "character_behavior" -> checkCharacterBehavior(storyId, req.manuscriptId());
            case "lore_check" -> checkLore(storyId, req.manuscriptId());
            default -> throw new IllegalArgumentException("Intent inconnu : " + req.intent());
        };
    }

    // ══════════════════════════════════════════════════════════════
    //  2. Conflits chronologiques (timeline_conflicts)
    //     Équivalent Python : timeline_date_conflicts()
    // ══════════════════════════════════════════════════════════════

    public SuggestionsResult findCharacterLinks(Long storyId) {
        List<StoryCharacter> chars = characterRepo.findByStoryIdOrderByNameAsc(storyId);
        List<CharacterLinkSuggestion> suggestions = new ArrayList<>();

        for (int i = 0; i < chars.size(); i++) {
            for (int j = i + 1; j < chars.size(); j++) {
                StoryCharacter a = chars.get(i);
                StoryCharacter b = chars.get(j);

                // Même nom de famille → lien familial
                if (a.getSurname() != null && b.getSurname() != null && a.getSurname().strip().equalsIgnoreCase(b.getSurname().strip())) {
                    suggestions.add(new CharacterLinkSuggestion("family", List.of(a.getId(), b.getId()), "Nom de famille commun : « " + a.getSurname() + " »"));
                }

                // Même génération (écart d'âge ≤ 5 ans)
                if (a.getAge() != null && b.getAge() != null && Math.abs(a.getAge() - b.getAge()) <= 5) {
                    suggestions.add(new CharacterLinkSuggestion("peer", List.of(a.getId(), b.getId()), "Même génération (écart : " + Math.abs(a.getAge() - b.getAge()) + " ans)"));
                }
            }
        }
        return new SuggestionsResult(suggestions);
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
                        conflicts.add(new TimelineConflict(ev.getId(), ch.getId(), ch.getName() + " n'est pas encore né(e) (" + ch.getBorn() + ") lors de l'événement (" + ev.getDate() + ")"));
                    }
                }
            }
        }
        return new ConflictsResult(conflicts);
    }

    // ══════════════════════════════════════════════════════════════
    //  3. Cohérence du script (script_consistency)
    //     Équivalent Python : find_mentions_in_doc + check_lore_relevance
    // ══════════════════════════════════════════════════════════════

    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) return null;
        try {
            return LocalDate.parse(dateStr);
        } catch (DateTimeParseException e) {
            // Dates au format narratif (ex: "Année 120") ignorées
            return null;
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  4. Comportement des personnages (character_behavior)
    //     Équivalent Python : analyze_behavior_syntax()
    // ══════════════════════════════════════════════════════════════

    public ScriptConsistencyResult checkScriptConsistency(Long storyId, Long manuscriptId) {
        String text = getManuscriptText(storyId, manuscriptId);

        // Mentions de personnages
        List<String> charNames = characterRepo.findByStoryIdOrderByNameAsc(storyId).stream().map(StoryCharacter::getName).filter(n -> n != null && !n.isBlank()).toList();
        Map<String, Integer> mentions = nlpService.findMentions(text, charNames);

        // Mentions du lore
        List<LoreEntry> loreEntries = loreRepo.findByStoryId(storyId);
        List<LoreMention> loreMentions = loreEntries.stream().filter(e -> e.getTitle() != null && text.toLowerCase().contains(e.getTitle().toLowerCase())).map(e -> new LoreMention(e.getId(), e.getTitle(), "mention", "Ce concept du Lore est mentionné. Vérifiez sa cohérence.")).toList();

        return new ScriptConsistencyResult(mentions, loreMentions);
    }

    public BehaviorResult checkCharacterBehavior(Long storyId, Long manuscriptId) {
        String text = getManuscriptText(storyId, manuscriptId).toLowerCase();
        List<StoryCharacter> chars = characterRepo.findByStoryIdOrderByNameAsc(storyId);
        List<BehaviorIssue> issues = new ArrayList<>();

        for (StoryCharacter ch : chars) {
            if (ch.getPersonality() == null) continue;
            String personality = ch.getPersonality().toLowerCase();
            String firstName = ch.getName() != null ? ch.getName().split(" ")[0].toLowerCase() : "";

            BEHAVIOR_RULES.forEach((trait, forbiddenActions) -> {
                if (!personality.contains(trait)) return;
                for (String action : forbiddenActions) {
                    // Recherche simplifiée : action présente + prénom du perso à proximité
                    int actionIdx = text.indexOf(action);
                    while (actionIdx != -1) {
                        int windowStart = Math.max(0, actionIdx - 100);
                        int windowEnd = Math.min(text.length(), actionIdx + 100);
                        String window = text.substring(windowStart, windowEnd);
                        if (!firstName.isBlank() && window.contains(firstName)) {
                            // Extraire la phrase de contexte
                            String context = extractSentence(text, actionIdx);
                            issues.add(new BehaviorIssue(ch.getId(), ch.getName(), action, trait, context, "Un personnage « " + trait + " » ne devrait pas « " + action + " »"));
                        }
                        actionIdx = text.indexOf(action, actionIdx + 1);
                    }
                }
            });
        }
        return new BehaviorResult(issues);
    }

    private String extractSentence(String text, int idx) {
        int start = text.lastIndexOf('.', idx);
        if (start < 0) start = 0;
        else start++;
        int end = text.indexOf('.', idx);
        if (end < 0) end = Math.min(text.length(), idx + 150);
        return text.substring(start, end).strip();
    }

    // ══════════════════════════════════════════════════════════════
    //  5. Vérification du Lore (lore_check)
    //     Équivalent Python : check_lore_relevance()
    // ══════════════════════════════════════════════════════════════

    public Map<String, List<LoreMention>> checkLore(Long storyId, Long manuscriptId) {
        String text = getManuscriptText(storyId, manuscriptId);
        List<LoreEntry> loreEntries = loreRepo.findByStoryId(storyId);

        List<LoreMention> analysis = loreEntries.stream().filter(e -> e.getTitle() != null && text.toLowerCase().contains(e.getTitle().toLowerCase())).map(e -> new LoreMention(e.getId(), e.getTitle(), "mention", "Ce concept du Lore est mentionné. Vérifiez sa cohérence.")).toList();

        return Map.of("loreAnalysis", analysis);
    }

    // ══════════════════════════════════════════════════════════════
    //  Utilitaire commun
    // ══════════════════════════════════════════════════════════════

    private String getManuscriptText(Long storyId, Long manuscriptId) {
        if (manuscriptId == null) {
            throw new IllegalArgumentException("manuscript_id requis pour l'analyse textuelle");
        }
        Manuscript m = manuscriptRepo.findById(manuscriptId).orElseThrow(() -> ResourceNotFoundException.of("Manuscrit", manuscriptId));
        return m.getText() != null ? m.getText() : "";
    }
}
