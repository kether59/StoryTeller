package com.kether.storyteller.service.llm;

import com.kether.storyteller.dto.request.Requests.*;
import com.kether.storyteller.dto.response.Responses.*;
import com.kether.storyteller.entity.*;
import com.kether.storyteller.exception.ResourceNotFoundException;
import com.kether.storyteller.repository.*;
import com.kether.storyteller.service.llm.LLMProviders.AnthropicProvider;
import com.kether.storyteller.service.llm.LLMProviders.OpenAIProvider;
import com.kether.storyteller.service.llm.LLMProviders.OpenRouterProvider;
import com.kether.storyteller.service.llm.LLMProviders.OllamaProvider;
import com.kether.storyteller.service.llm.LLMProviders.GeminiProvider;
import com.kether.storyteller.service.llm.LLMProviders.LMStudioProvider;
import com.kether.storyteller.service.llm.LLMProviders.LlamaCPPProvider;
import com.kether.storyteller.service.llm.LLMProviders.LLMProvider;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

/**
 * Orchestrateur LLM â€“ Ã©quivalent combinÃ© de llm.py + call_llm_configured() Python.
 *
 * Routes gÃ©rÃ©es :
 *   POST /api/llm/generate-chapter
 *   POST /api/llm/continue-writing
 *   POST /api/llm/rewrite
 *   POST /api/llm/suggest-next-scene
 */
@Service
public class LLMService {

    private static final Logger log = LoggerFactory.getLogger(LLMService.class);

    private final LLMConfigService      configService;
    private final AnthropicProvider     anthropic;
    private final OpenAIProvider        openai;
    private final OpenRouterProvider    openrouter;
    private final OllamaProvider        ollama;
    private final GeminiProvider        geminiProvider;
    private final LMStudioProvider      lMStudio;
    private final LlamaCPPProvider      llamacpp;

    private final ObjectMapper mapper;

    private final StoryRepository storyRepo;
    private final CharacterRepository characterRepo;
    private final LocationRepository locationRepo;
    private final TimelineEventRepository timelineRepo;
    private final LoreEntryRepository loreRepo;
    private final ManuscriptRepository manuscriptRepo;

    public LLMService(LLMConfigService configService,
                      AnthropicProvider anthropic, OpenAIProvider openai,
                      OpenRouterProvider openrouter, OllamaProvider ollama,
                      GeminiProvider geminiProvider,
                      LMStudioProvider lMStudio,
                      LlamaCPPProvider llamacpp,
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
        this.geminiProvider = geminiProvider;
        this.lMStudio       = lMStudio;
        this.llamacpp       = llamacpp;
        this.mapper         = mapper;
        this.storyRepo      = storyRepo;
        this.characterRepo  = characterRepo;
        this.locationRepo   = locationRepo;
        this.timelineRepo   = timelineRepo;
        this.loreRepo       = loreRepo;
        this.manuscriptRepo = manuscriptRepo;
    }

    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
    //  Appel LLM gÃ©nÃ©rique (Ã©quivalent call_llm_configured Python)
    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•

    public String callLLM(String systemPrompt, String userPrompt) throws Exception {
        return callLLM(systemPrompt, userPrompt, configService.getCurrent().getMaxTokens());
    }

    public String callLLM(String systemPrompt, String userPrompt, int maxTokens) throws Exception {
        var cfg = configService.getCurrent();
        var provider = resolveProvider(cfg.getProvider());
        return provider.call(systemPrompt, userPrompt, maxTokens, cfg);
    }

    private LLMProvider resolveProvider(String name) {
        return switch (name != null ? name.toLowerCase() : "") {
            case "anthropic" -> anthropic;
            case "openai"    -> openai;
            case "openrouter"-> openrouter;
            case "ollama"    -> ollama;
            case "gemini"    -> geminiProvider;
            case "lmstudio"  -> lMStudio;
            case "llama"     -> llamacpp;
            default -> throw new IllegalArgumentException("Provider inconnu : " + name);
        };
    }

    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
    //  Test de connexion (POST /api/llm/test)
    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•

    public LLMTestResponse testConnection(LLMTestRequest req) {
        var tmpCfg = new LLMConfigModel();
        tmpCfg.setProvider(req.provider());
        tmpCfg.setModel(req.model() != null && !req.model().isBlank()
                ? req.model()
                : getDefaultModelForProvider(req.provider()));

        tmpCfg.setApiKey(req.apiKey());

        String ollamaUrl = req.ollamaUrl();
        if ("llama".equalsIgnoreCase(req.provider()) || "ollama".equalsIgnoreCase(req.provider())) {
            if (ollamaUrl == null || ollamaUrl.contains("127.0.0.1") || ollamaUrl.contains("localhost")) {
                ollamaUrl = configService.getCurrent().getOllamaUrl();
                if (ollamaUrl == null || ollamaUrl.isBlank()) {
                    ollamaUrl = "http://llama-cpp:8080";
                }
            }
        }

        tmpCfg.setOllamaUrl(ollamaUrl);
        tmpCfg.setLmstudioUrl(req.lmstudioUrl() != null && !req.lmstudioUrl().isBlank()
                ? req.lmstudioUrl()
                : "http://localhost:1234");
        tmpCfg.setGeminiApiKey(req.geminiApiKey());

        try {
            String response = resolveProvider(req.provider()).test(tmpCfg);
            return new LLMTestResponse(true, "âœ… Connexion rÃ©ussie avec le modÃ¨le " + tmpCfg.getModel());
        } catch (Exception e) {
            return new LLMTestResponse(false, "âŒ " + e.getMessage());
        }
    }

    private String getDefaultModelForProvider(String provider) {
        return switch (provider) {
            case "ollama" -> "mistral";
            case "lmstudio" -> "local-model";
            case "anthropic" -> "claude-sonnet-4-5";
            case "openai" -> "gpt-4o";
            default -> "default";
        };
    }

    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
    //  GÃ©nÃ©ration de chapitre (POST /api/llm/generate-chapter)
    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•

    public GeneratedChapterResponse generateChapter(ChapterGenerationRequest req) {
        log.info("generateChapter called â€” storyId={}, chapterNumber={}, title={}", req.storyId(), req.chapterNumber(), req.chapterTitle());

        Story story = storyRepo.findById(req.storyId())
                .orElseThrow(() -> new ResourceNotFoundException("Histoire introuvable"));

        List<StoryCharacter> allChars  = characterRepo.findByStoryId(req.storyId());
        List<StoryLocation>  allLocs   = locationRepo.findByStoryIdOrderByNameAsc(req.storyId());
        List<TimelineEvent>  timeline  = timelineRepo.findByStoryId(req.storyId());
        List<LoreEntry>      lore      = loreRepo.findByStoryId(req.storyId());

        // Filtrer les persos/lieux demandÃ©s
        List<StoryCharacter> selectedChars = req.includeCharacters() != null
                ? allChars.stream().filter(c -> req.includeCharacters().contains(c.getId())).toList()
                : List.of();
        List<StoryLocation> selectedLocs = req.includeLocations() != null
                ? allLocs.stream().filter(l -> req.includeLocations().contains(l.getId())).toList()
                : List.of();

        // Try to extract style reference from the last manuscript of the story
        String styleReference = null;
        try {
            List<Manuscript> manuscripts = manuscriptRepo.findByStoryIdOrderByChapterDesc(req.storyId());
            if (!manuscripts.isEmpty()) {
                String lastText = manuscripts.get(0).getText();
                styleReference = extractStyleReference(
                        lastText != null ? lastText : "", 
                        3000
                );
            }
        } catch (Exception e) {
            log.warn("Could not extract style reference from previous manuscript", e);
            // Continue without style reference
        }

        String systemPrompt = buildSystemPromptWithStyleReference(story, allChars, allLocs, timeline, lore, styleReference);

        var userSb = new StringBuilder();
        userSb.append("Ã‰cris un nouveau chapitre pour ce roman.\n\n");
        userSb.append("Chapitre ").append(req.chapterNumber() != null ? req.chapterNumber() : "?");
        if (req.chapterTitle() != null) {
            userSb.append(" - ").append(req.chapterTitle());
        }
        userSb.append("\n\n");

        userSb.append("RÃ©sumÃ© narratif :\n").append(req.summary()).append("\n\n");

        if (!selectedChars.isEmpty()) {
            userSb.append("Personnages clÃ©s :\n");
            selectedChars.forEach(c -> userSb.append("- ").append(c.getName())
                    .append(c.getSurname() != null ? " " + c.getSurname() : "").append("\n"));
            userSb.append("\n");
        }
        
        if (!selectedLocs.isEmpty()) {
            userSb.append("Lieux :\n");
            selectedLocs.forEach(l -> userSb.append("- ").append(l.getName()).append("\n"));
            userSb.append("\n");
        }

        int targetWords = 1500;
        if (req.length() != null) {
            targetWords = switch (req.length()) {
                case "court" -> 800;
                case "long" -> 3000;
                default -> 1500;
            };
        }

        userSb.append("Ã‰cris environ ").append(targetWords).append(" mots.");

        try {
            log.debug("Calling LLM â€” provider={}, model={}, systemPromptLen={}, userPromptLen={}",
                    configService.getCurrent().getProvider(), configService.getCurrent().getModel(),
                    systemPrompt != null ? systemPrompt.length() : 0,
                    userSb.length());

            String text = callLLM(systemPrompt, userSb.toString(), 4000);
            int wordCount = text != null ? text.split("\\s+").length : 0;

            log.info("Generation successful â€” storyId={}, words={}", req.storyId(), wordCount);
            return new GeneratedChapterResponse(true, text,
                    req.chapterNumber(), req.chapterTitle(), wordCount);
        } catch (Exception e) {
            log.error("Error during chapter generation for storyId={}", req.storyId(), e);
            String msg = e.getMessage() != null ? e.getMessage() : e.toString();
            throw new RuntimeException("Erreur lors de la gÃ©nÃ©ration : " + e.getClass().getSimpleName() + " - " + msg, e);
        }
    }

    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
    //  Continuation (POST /api/llm/continue-writing)
    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•

    public ContinuationResponse continueWriting(ContinueWritingRequest req) {
        log.info("continueWriting called â€” manuscriptId={}, length={}", req.manuscriptId(), req.length());
        Manuscript manuscript = manuscriptRepo.findById(req.manuscriptId())
                .orElseThrow(() -> new ResourceNotFoundException("Manuscrit introuvable"));

        List<StoryCharacter> chars    = characterRepo.findByStoryId(manuscript.getStory().getId());
        List<StoryLocation>  locs     = locationRepo.findByStoryIdOrderByNameAsc(manuscript.getStory().getId());
        List<TimelineEvent>  timeline = timelineRepo.findByStoryId(manuscript.getStory().getId());
        List<LoreEntry>      lore     = loreRepo.findByStoryId(manuscript.getStory().getId());

        // Extract style reference from the last 3000 words of the manuscript
        String styleReference = extractStyleReference(
                manuscript.getText() != null ? manuscript.getText() : "", 
                3000
        );

        String systemPrompt = buildSystemPromptWithStyleReference(
                manuscript.getStory(), chars, locs, timeline, lore, styleReference
        );

        String userPrompt = """
                Continue le chapitre "%s".

                Direction narrative : %s

                Ã‰cris environ %d mots supplÃ©mentaires qui s'intÃ¨grent naturellement Ã  la suite.
                Ne rÃ©pÃ¨te pas ce qui a dÃ©jÃ  Ã©tÃ© Ã©crit.
                Commence directement la suite sans prÃ©ambule ni explication.
                """.formatted(
                manuscript.getTitle(),
                req.direction(),
                req.length() != null ? req.length() : 500
        );

        try {
            log.debug("Calling LLM for continuation â€” provider={}, model={}", configService.getCurrent().getProvider(), configService.getCurrent().getModel());
            String continuation = callLLM(systemPrompt, userPrompt, 2000);
            int words = continuation != null ? continuation.split("\\s+").length : 0;
            log.info("Continuation successful â€” manuscriptId={}, words={}", req.manuscriptId(), words);
            return new ContinuationResponse(true, continuation, words);
        } catch (Exception e) {
            log.error("Error during continuation for manuscriptId={}", req.manuscriptId(), e);
            String msg = e.getMessage() != null ? e.getMessage() : e.toString();
            throw new RuntimeException("Erreur lors de la continuation : " + e.getClass().getSimpleName() + " - " + msg, e);
        }
    }

    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
    //  RÃ©Ã©criture (POST /api/llm/rewrite)
    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•

    public RewriteResponse rewrite(RewriteRequest req) {
        log.info("rewrite called â€” instruction length={}, originalTextLen={}",
                req.instruction() != null ? req.instruction().length() : 0,
                req.text() != null ? req.text().length() : 0);

        String systemPrompt = """
                Tu es un Ã©crivain professionnel expert en rÃ©vision et rÃ©Ã©criture de textes.

                Ton rÃ´le est de rÃ©Ã©crire les passages proposÃ©s selon les instructions donnÃ©es,
                tout en prÃ©servant l'essence, le sens original et la voix de l'auteur.

                Ne jamais ajouter de commentaires ou d'explications.
                La rÃ©ponse contient uniquement le texte rÃ©Ã©crit.
                """;

        String userPrompt = """
                RÃ©Ã©cris ce passage selon ces instructions :

                Instructions : %s

                Passage original :

                %s

                Fournis uniquement le texte rÃ©Ã©crit, sans explications.
                """.formatted(req.instruction(), req.text());

        try {
            log.debug("Calling LLM for rewrite â€” provider={}, model={}", configService.getCurrent().getProvider(), configService.getCurrent().getModel());
            String rewritten = callLLM(systemPrompt, userPrompt, 2000);
            log.info("Rewrite successful â€” originalLen={}, rewrittenLen={}", req.text() != null ? req.text().length() : 0, rewritten != null ? rewritten.length() : 0);
            return new RewriteResponse(true, req.text(), rewritten, req.instruction());
        } catch (Exception e) {
            log.error("Error during rewrite", e);
            String msg = e.getMessage() != null ? e.getMessage() : e.toString();
            throw new RuntimeException("Erreur lors de la rÃ©Ã©criture : " + e.getClass().getSimpleName() + " - " + msg, e);
        }
    }

    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
    //  Suggestions de scÃ¨nes (POST /api/llm/suggest-next-scene)
    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•

    public SuggestionsResponse suggestNextScene(SuggestNextSceneRequest req) {
        log.info("suggestNextScene called â€” storyId={}", req.storyId());
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

                SuggÃ¨re 5 idÃ©es diffÃ©rentes pour la prochaine scÃ¨ne.
                Pour chaque idÃ©e, donne :
                1. Un titre accrocheur
                2. Une description en 2-3 phrases
                3. Les personnages impliquÃ©s
                4. L'impact potentiel sur l'intrigue

                Formate ta rÃ©ponse en JSON valide :
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
            log.debug("Calling LLM for suggestions â€” provider={}, model={}", configService.getCurrent().getProvider(), configService.getCurrent().getModel());
            String raw = callLLM(systemPrompt, userPrompt, 2000);
            // Nettoyage des backticks Markdown Ã©ventuels
            String cleaned = raw != null ? raw.strip()
                    .replaceAll("(?s)^```json\\s*", "")
                    .replaceAll("(?s)^```\\s*", "")
                    .replaceAll("(?s)\\s*```$", "")
                    .strip() : "";

            Map<String, List<SceneSuggestion>> parsed = mapper.readValue(
                    cleaned, new TypeReference<>() {});
            log.info("Suggestions parsed â€” count={}", parsed.getOrDefault("suggestions", List.of()).size());
            return new SuggestionsResponse(parsed.getOrDefault("suggestions", List.of()));
        } catch (Exception e) {
            log.error("Error during suggestNextScene for storyId={}", req.storyId(), e);
            // Fallback : retourner la rÃ©ponse brute
            String msg = e.getMessage() != null ? e.getMessage() : e.toString();
            return new SuggestionsResponse(List.of(
                    new SceneSuggestion("RÃ©ponse brute", msg, List.of(), "")
            ));
        }
    }

    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
    //  Construction du prompt systÃ¨me (Ã©quivalent build_system_prompt Python)
    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•

    private String buildSystemPrompt(Story story,
                                     List<StoryCharacter> characters,
                                     List<StoryLocation> locations,
                                     List<TimelineEvent> timeline,
                                     List<LoreEntry> lore) {
        return buildSystemPromptWithStyleReference(story, characters, locations, timeline, lore, null);
    }

    private String buildSystemPromptWithStyleReference(Story story,
                                                       List<StoryCharacter> characters,
                                                       List<StoryLocation> locations,
                                                       List<TimelineEvent> timeline,
                                                       List<LoreEntry> lore,
                                                       String styleReference) {
        var sb = new StringBuilder();

        sb.append("""
                Tu es un Ã©crivain professionnel spÃ©cialisÃ© dans le roman.

                Tu participes Ã  l'Ã©criture du roman intitulÃ© :

                Â« """).append(story.getTitle()).append(" Â»\n\n");

        sb.append("""
                Tu n'es pas un assistant conversationnel.
                Tu n'expliques jamais tes choix.
                Tu ne commentes jamais ton texte.
                Tu Ã©cris directement le roman.

                ========================
                OBJECTIF
                ========================

                Ton objectif est de produire un texte qui semble avoir Ã©tÃ© Ã©crit par le mÃªme auteur que les chapitres prÃ©cÃ©dents.

                Le lecteur ne doit jamais percevoir de rupture de style, de rythme ou de qualitÃ©.

                ========================
                COHÃ‰RENCE
                ========================

                Respecte strictement :

                """);

        sb.append("- Le synopsis : ").append(story.getSynopsis() != null ? story.getSynopsis() : "Non dÃ©fini").append("\n");
        sb.append("- Les personnages, leurs motivations, connaissances, personnalitÃ© et Ã©motions\n");
        sb.append("- Les lieux et leur description\n");
        sb.append("- Le lore et la chronologie\n");
        sb.append("- Les Ã©vÃ©nements dÃ©jÃ  Ã©crits\n\n");

        sb.append("Tu n'inventes jamais un Ã©lÃ©ment qui contredit ces informations.\n");
        sb.append("En cas de doute, privilÃ©gie toujours la cohÃ©rence.\n\n");

        sb.append("""
                ========================
                PERSONNAGES
                ========================

                """);

        for (var c : characters) {
            sb.append("### ").append(c.getName())
                    .append(c.getSurname() != null ? " " + c.getSurname() : "").append("\n");
            if (c.getRole()        != null) sb.append("- RÃ´le: ").append(c.getRole()).append("\n");
            if (c.getAge()         != null) sb.append("- Ã‚ge: ").append(c.getAge()).append(" ans\n");
            if (c.getPersonality() != null) sb.append("- PersonnalitÃ©: ").append(c.getPersonality()).append("\n");
            if (c.getMotivation()  != null) sb.append("- Motivation: ").append(c.getMotivation()).append("\n");
            sb.append("\n");
        }

        if (!locations.isEmpty()) {
            sb.append("========================\n");
            sb.append("LIEUX\n");
            sb.append("========================\n\n");
            for (var l : locations) {
                sb.append("### ").append(l.getName());
                if (l.getType()    != null) sb.append(" (").append(l.getType()).append(")");
                sb.append("\n");
                if (l.getSummary() != null) sb.append(l.getSummary()).append("\n");
                sb.append("\n");
            }
        }

        if (!lore.isEmpty()) {
            sb.append("========================\n");
            sb.append("LORE ET MONDES\n");
            sb.append("========================\n\n");
            for (var e : lore) {
                sb.append("### ").append(e.getTitle());
                if (e.getCategory() != null) sb.append(" - ").append(e.getCategory());
                sb.append("\n");
                if (e.getContent()  != null) sb.append(e.getContent()).append("\n");
                sb.append("\n");
            }
        }

        if (!timeline.isEmpty()) {
            sb.append("========================\n");
            sb.append("CHRONOLOGIE\n");
            sb.append("========================\n\n");
            int limit = Math.min(timeline.size(), 10);
            for (int i = 0; i < limit; i++) {
                var ev = timeline.get(i);
                sb.append("- ").append(ev.getTitle());
                if (ev.getDate()    != null) sb.append(" (").append(ev.getDate()).append(")");
                if (ev.getSummary() != null) sb.append(": ").append(ev.getSummary());
                sb.append("\n");
            }
            sb.append("\n");
        }

        if (styleReference != null && !styleReference.isBlank()) {
            sb.append("========================\n");
            sb.append("STYLE DE RÃ‰FÃ‰RENCE\n");
            sb.append("========================\n\n");
            sb.append("Voici un extrait du chapitre prÃ©cÃ©dent.\n");
            sb.append("Il reprÃ©sente la rÃ©fÃ©rence absolue pour le style.\n");
            sb.append("Reproduis exactement son style d'Ã©criture :\n\n");
            sb.append(styleReference).append("\n\n");
            sb.append("========================\n");
            sb.append("FIN DU STYLE DE RÃ‰FÃ‰RENCE\n");
            sb.append("========================\n\n");
        }

        sb.append("""
                ========================
                STYLE D'Ã‰CRITURE
                ========================

                Ã‰cris comme un vÃ©ritable romancier.

                Le texte doit Ãªtre :
                - naturel et fluide
                - immersif et crÃ©dible
                - vivant et authentique

                Ã‰vite :
                - les rÃ©pÃ©titions et les clichÃ©s
                - les phrases artificielles
                - les explications inutiles
                - les rÃ©sumÃ©s et les listes

                Montre les Ã©vÃ©nements au lieu de les raconter.

                PrivilÃ©gie :
                - les actions et les sensations
                - les Ã©motions et les rÃ©actions
                - les dialogues crÃ©dibles et naturels

                Les dialogues doivent avoir une voix propre Ã  chaque personnage.

                Alterne naturellement narration, dialogue, introspection et action.

                ========================
                INTERDICTIONS
                ========================

                Ne jamais Ã©crire :
                - "Voici le chapitre"
                - "Chapitre :"
                - "J'espÃ¨re que..."
                - "Bonne lecture"
                - "Suite :"
                - "En rÃ©sumÃ©"

                Ne jamais commenter le texte.
                Ne jamais expliquer tes choix.
                Ne jamais sortir du roman.
                Ne jamais produire de Markdown.
                Ne jamais utiliser de listes.
                Ne jamais parler au lecteur.

                ========================
                SORTIE
                ========================

                La rÃ©ponse contient uniquement le texte du roman.
                Rien d'autre.
                """);

        return sb.toString();
    }

    private String extractStyleReference(String fullText, int targetWords) {
        if (fullText == null || fullText.isBlank()) {
            return null;
        }

        String[] words = fullText.split("\\s+");
        int startIndex = Math.max(0, words.length - targetWords);

        StringBuilder result = new StringBuilder();
        for (int i = startIndex; i < words.length; i++) {
            if (result.length() > 0) result.append(" ");
            result.append(words[i]);
        }

        return result.toString().isBlank() ? null : result.toString();
    }
}