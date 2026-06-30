package com.kether.storyteller.service.llm;

import com.kether.storyteller.dto.request.Requests.*;
import com.kether.storyteller.dto.response.Responses.*;
import com.kether.storyteller.entity.*;
import com.kether.storyteller.entity.*;
import com.kether.storyteller.exception.ResourceNotFoundException;
import com.kether.storyteller.repository.*;
import com.kether.storyteller.service.llm.LLMProviders.AnthropicProvider;
import com.kether.storyteller.service.llm.LLMProviders.OpenAIProvider;
import com.kether.storyteller.service.llm.LLMProviders.OpenRouterProvider;
import com.kether.storyteller.service.llm.LLMProviders.OllamaProvider;
import com.kether.storyteller.service.llm.LLMProviders.LLMProvider;
import org.springframework.stereotype.Service;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

/**
 * Orchestrateur LLM – équivalent combiné de llm.py + call_llm_configured() Python.
 *
 * Routes gérées :
 *   POST /api/llm/generate-chapter
 *   POST /api/llm/continue-writing
 *   POST /api/llm/rewrite
 *   POST /api/llm/suggest-next-scene
 */
@Service
public class LLMService {

    private final LLMConfigService      configService;
    private final AnthropicProvider     anthropic;
    private final OpenAIProvider        openai;
    private final OpenRouterProvider    openrouter;
    private final OllamaProvider        ollama;
    private final ObjectMapper mapper;

    private final StoryRepository       storyRepo;
    private final CharacterRepository   characterRepo;
    private final LocationRepository    locationRepo;
    private final TimelineEventRepository timelineRepo;
    private final LoreEntryRepository   loreRepo;
    private final ManuscriptRepository  manuscriptRepo;

    public LLMService(LLMConfigService configService,
                      AnthropicProvider anthropic, OpenAIProvider openai,
                      OpenRouterProvider openrouter, OllamaProvider ollama,
                      ObjectMapper mapper,
                      StoryRepository storyRepo,
                      CharacterRepository characterRepo,
                      LocationRepository locationRepo,
                      TimelineEventRepository timelineRepo,
                      LoreEntryRepository loreRepo,
                      ManuscriptRepository manuscriptRepo) {
        this.configService  = configService;
        this.anthropic      = anthropic;
        this.openai         = openai;
        this.openrouter     = openrouter;
        this.ollama         = ollama;
        this.mapper         = mapper;
        this.storyRepo      = storyRepo;
        this.characterRepo  = characterRepo;
        this.locationRepo   = locationRepo;
        this.timelineRepo   = timelineRepo;
        this.loreRepo       = loreRepo;
        this.manuscriptRepo = manuscriptRepo;
    }

    // ══════════════════════════════════════════════════════════════
    //  Appel LLM générique (équivalent call_llm_configured Python)
    // ══════════════════════════════════════════════════════════════

    public String callLLM(String systemPrompt, String userPrompt) throws Exception {
        return callLLM(systemPrompt, userPrompt, configService.getCurrent().getMaxTokens());
    }

    public String callLLM(String systemPrompt, String userPrompt, int maxTokens) throws Exception {
        var cfg = configService.getCurrent();
        var provider = resolveProvider(cfg.getProvider());
        return provider.call(systemPrompt, userPrompt, maxTokens, cfg);
    }

    private LLMProvider resolveProvider(String name) {
        return switch (name) {
            case "anthropic"  -> anthropic;
            case "openai"     -> openai;
            case "openrouter" -> openrouter;
            case "ollama"     -> ollama;
            default -> throw new IllegalArgumentException(
                    "Provider inconnu : " + name + ". Valeurs valides : anthropic, openai, openrouter, ollama");
        };
    }

    // ══════════════════════════════════════════════════════════════
    //  Test de connexion (POST /api/llm/test)
    // ══════════════════════════════════════════════════════════════

    public LLMTestResponse testConnection(LLMTestRequest req) {
        var tmpCfg = new LLMConfigModel();
        tmpCfg.setProvider(req.provider());
        tmpCfg.setModel(req.model());
        tmpCfg.setApiKey(req.apiKey() != null ? req.apiKey() : "");
        tmpCfg.setOllamaUrl(req.ollamaUrl() != null ? req.ollamaUrl()
                : "http://localhost:11434");
        try {
            String response = resolveProvider(req.provider()).test(tmpCfg);
            return new LLMTestResponse(true, "Connexion réussie. Réponse : « " + response.strip() + " »");
        } catch (Exception e) {
            return new LLMTestResponse(false, "Erreur de connexion : " + e.getMessage());
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  Génération de chapitre (POST /api/llm/generate-chapter)
    // ══════════════════════════════════════════════════════════════

    public GeneratedChapterResponse generateChapter(ChapterGenerationRequest req) {
        Story story = storyRepo.findById(req.storyId())
                .orElseThrow(() -> new ResourceNotFoundException("Histoire introuvable"));

        List<StoryCharacter> allChars  = characterRepo.findByStoryId(req.storyId());
        List<StoryLocation>  allLocs   = locationRepo.findByStoryIdOrderByNameAsc(req.storyId());
        List<TimelineEvent>  timeline  = timelineRepo.findByStoryId(req.storyId());
        List<LoreEntry>      lore      = loreRepo.findByStoryId(req.storyId());

        // Filtrer les persos/lieux demandés
        List<StoryCharacter> selectedChars = req.includeCharacters() != null
                ? allChars.stream().filter(c -> req.includeCharacters().contains(c.getId())).toList()
                : List.of();
        List<StoryLocation> selectedLocs = req.includeLocations() != null
                ? allLocs.stream().filter(l -> req.includeLocations().contains(l.getId())).toList()
                : List.of();

        String systemPrompt = buildSystemPrompt(story, allChars, allLocs, timeline, lore);

        Map<String, String> lengthGuide = Map.of(
                "court", "500-800 mots",
                "moyen", "1000-1500 mots",
                "long",  "2000-3000 mots"
        );

        var sb = new StringBuilder();
        sb.append("Écris un chapitre pour ce roman.\n\n");
        sb.append("## Informations du chapitre\n");
        sb.append("- Numéro: ").append(req.chapterNumber() != null ? req.chapterNumber() : "À définir").append("\n");
        sb.append("- Titre: ").append(req.chapterTitle() != null ? req.chapterTitle() : "À générer").append("\n");
        sb.append("- Longueur souhaitée: ").append(lengthGuide.getOrDefault(req.length(), "1000-1500 mots")).append("\n");
        sb.append("- Style: ").append(req.style() != null ? req.style() : "narratif").append("\n");
        sb.append("- Ton: ").append(req.tone() != null ? req.tone() : "neutre").append("\n");
        sb.append("- Point de vue: ").append(req.pov() != null ? req.pov() : "troisième personne").append("\n\n");
        sb.append("## Ce qui doit se passer\n").append(req.summary()).append("\n");

        if (!selectedChars.isEmpty()) {
            sb.append("\n## Personnages à inclure\n");
            selectedChars.forEach(c -> sb.append("- ").append(c.getName())
                    .append(c.getSurname() != null ? " " + c.getSurname() : "").append("\n"));
        }
        if (!selectedLocs.isEmpty()) {
            sb.append("\n## Lieux à utiliser\n");
            selectedLocs.forEach(l -> sb.append("- ").append(l.getName()).append("\n"));
        }
        sb.append("\n\nÉcris maintenant le chapitre complet en respectant toutes ces consignes.");

        try {
            String text = callLLM(systemPrompt, sb.toString(), 4000);
            int wordCount = text.split("\\s+").length;
            return new GeneratedChapterResponse(true, text,
                    req.chapterNumber(), req.chapterTitle(), wordCount);
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la génération : " + e.getMessage(), e);
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  Continuation (POST /api/llm/continue-writing)
    // ══════════════════════════════════════════════════════════════

    public ContinuationResponse continueWriting(ContinueWritingRequest req) {
        Manuscript manuscript = manuscriptRepo.findById(req.manuscriptId())
                .orElseThrow(() -> new ResourceNotFoundException("Manuscrit introuvable"));

        List<StoryCharacter> chars    = characterRepo.findByStoryId(manuscript.getStory().getId());
        List<StoryLocation>  locs     = locationRepo.findByStoryIdOrderByNameAsc(manuscript.getStory().getId());
        List<TimelineEvent>  timeline = timelineRepo.findByStoryId(manuscript.getStory().getId());
        List<LoreEntry>      lore     = loreRepo.findByStoryId(manuscript.getStory().getId());

        String systemPrompt = buildSystemPrompt(manuscript.getStory(), chars, locs, timeline, lore);

        String userPrompt = """
                Voici le texte actuel du chapitre "%s":

                %s

                ---

                Continue cette histoire dans cette direction : %s

                Écris environ %d mots supplémentaires qui s'intègrent naturellement à la suite.
                Ne répète pas ce qui a déjà été écrit. Commence directement la suite sans préambule.
                """.formatted(
                manuscript.getTitle(),
                manuscript.getText() != null ? manuscript.getText() : "",
                req.direction(),
                req.length() != null ? req.length() : 500
        );

        try {
            String continuation = callLLM(systemPrompt, userPrompt, 2000);
            return new ContinuationResponse(true, continuation, continuation.split("\\s+").length);
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la continuation : " + e.getMessage(), e);
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  Réécriture (POST /api/llm/rewrite)
    // ══════════════════════════════════════════════════════════════

    public RewriteResponse rewrite(RewriteRequest req) {
        String system = "Tu es un assistant d'écriture expert. Tu réécris des textes selon les instructions données "
                + "tout en préservant l'essence et le sens original.";
        String user = """
                Voici le texte à réécrire:

                %s

                ---

                Instructions de réécriture : %s

                Réécris maintenant le texte en suivant ces instructions.
                """.formatted(req.text(), req.instruction());

        try {
            String rewritten = callLLM(system, user, 2000);
            return new RewriteResponse(true, req.text(), rewritten, req.instruction());
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la réécriture : " + e.getMessage(), e);
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  Suggestions de scènes (POST /api/llm/suggest-next-scene)
    // ══════════════════════════════════════════════════════════════

    public SuggestionsResponse suggestNextScene(SuggestNextSceneRequest req) {
        Story story = storyRepo.findById(req.storyId())
                .orElseThrow(() -> new ResourceNotFoundException("Histoire introuvable"));

        List<StoryCharacter> chars    = characterRepo.findByStoryId(req.storyId());
        List<StoryLocation>  locs     = locationRepo.findByStoryIdOrderByNameAsc(req.storyId());
        List<TimelineEvent>  timeline = timelineRepo.findByStoryId(req.storyId());
        List<LoreEntry>      lore     = loreRepo.findByStoryId(req.storyId());

        String systemPrompt = buildSystemPrompt(story, chars, locs, timeline, lore);

        String userPrompt = """
                Situation actuelle dans l'histoire :
                %s

                Suggère 5 idées différentes pour la prochaine scène.
                Pour chaque idée, donne :
                1. Un titre accrocheur
                2. Une description en 2-3 phrases
                3. Les personnages impliqués
                4. L'impact potentiel sur l'intrigue

                Formate ta réponse en JSON valide :
                {
                  "suggestions": [
                    {
                      "title": "...",
                      "description": "...",
                      "characters": ["...", "..."],
                      "impact": "..."
                    }
                  ]
                }
                """.formatted(req.currentSituation());

        try {
            String raw = callLLM(systemPrompt, userPrompt, 2000);
            // Nettoyage des backticks Markdown éventuels
            String cleaned = raw.strip()
                    .replaceAll("(?s)^```json\\s*", "")
                    .replaceAll("(?s)^```\\s*", "")
                    .replaceAll("(?s)\\s*```$", "")
                    .strip();

            Map<String, List<SceneSuggestion>> parsed = mapper.readValue(
                    cleaned, new TypeReference<>() {});
            return new SuggestionsResponse(parsed.getOrDefault("suggestions", List.of()));
        } catch (Exception e) {
            // Fallback : retourner la réponse brute
            return new SuggestionsResponse(List.of(
                    new SceneSuggestion("Réponse brute", e.getMessage(), List.of(), "")
            ));
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  Construction du prompt système (équivalent build_system_prompt Python)
    // ══════════════════════════════════════════════════════════════

    private String buildSystemPrompt(Story story,
                                     List<StoryCharacter> characters,
                                     List<StoryLocation> locations,
                                     List<TimelineEvent> timeline,
                                     List<LoreEntry> lore) {
        var sb = new StringBuilder();
        sb.append("Tu es un assistant d'écriture créative expert. Tu aides à rédiger un roman intitulé \"")
                .append(story.getTitle()).append("\".\n\n");

        sb.append("## Synopsis de l'histoire\n")
                .append(story.getSynopsis() != null ? story.getSynopsis() : "Non défini")
                .append("\n\n## Personnages principaux\n");

        for (var c : characters) {
            sb.append("\n### ").append(c.getName())
                    .append(c.getSurname() != null ? " " + c.getSurname() : "").append("\n");
            if (c.getRole()        != null) sb.append("- Rôle: ").append(c.getRole()).append("\n");
            if (c.getAge()         != null) sb.append("- Âge: ").append(c.getAge()).append(" ans\n");
            if (c.getPersonality() != null) sb.append("- Personnalité: ").append(c.getPersonality()).append("\n");
            if (c.getMotivation()  != null) sb.append("- Motivation: ").append(c.getMotivation()).append("\n");
        }

        if (!locations.isEmpty()) {
            sb.append("\n## Lieux importants\n");
            for (var l : locations) {
                sb.append("\n### ").append(l.getName());
                if (l.getType()    != null) sb.append(" (").append(l.getType()).append(")");
                if (l.getSummary() != null) sb.append("\n").append(l.getSummary());
                sb.append("\n");
            }
        }

        if (!lore.isEmpty()) {
            sb.append("\n## Éléments du monde (Lore)\n");
            for (var e : lore) {
                sb.append("\n### ").append(e.getTitle());
                if (e.getCategory() != null) sb.append(" - ").append(e.getCategory());
                if (e.getContent()  != null) sb.append("\n").append(e.getContent());
                sb.append("\n");
            }
        }

        if (!timeline.isEmpty()) {
            sb.append("\n## Chronologie des événements\n");
            int limit = Math.min(timeline.size(), 10);
            for (int i = 0; i < limit; i++) {
                var ev = timeline.get(i);
                sb.append("\n- ").append(ev.getTitle());
                if (ev.getDate()    != null) sb.append(" (").append(ev.getDate()).append(")");
                if (ev.getSummary() != null) sb.append(": ").append(ev.getSummary());
            }
            sb.append("\n");
        }

        sb.append("""

                ## Instructions
                - Respecte la personnalité et les motivations des personnages
                - Utilise les éléments du lore de manière cohérente
                - Maintiens le ton et le style de l'univers
                - Écris en français avec un style littéraire de qualité
                - Crée des scènes vivantes avec descriptions et dialogues naturels
                """);

        return sb.toString();
    }
}