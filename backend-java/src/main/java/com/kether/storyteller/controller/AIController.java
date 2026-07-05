package com.kether.storyteller.controller;

import com.kether.storyteller.dto.request.Requests.AIAnalysisRequest;
import com.kether.storyteller.service.AIService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * Contrôleur IA – équivalent ai.py.
 *
 * Route : POST /api/ai/suggest
 *
 * Le paramètre {@code storyYd} est ajouté en query param car la version Java
 * ne dispose pas du contexte de session Python – le frontend doit le passer.
 * Pour conserver la compatibilité totale avec le frontend React existant,
 * storyId peut aussi être extrait de manuscriptId via le service.
 */
@RestController
@RequestMapping("/api/ai")
public class AIController {

    private final AIService aiService;

    public AIController(AIService aiService) {
        this.aiService = aiService;
    }

    /**
     * POST /api/ai/suggest
     *
     * Body : { "intent": "link_characters|timeline_conflicts|...", "manuscript_id": 1 }
     * Query: ?story_id=1
     *
     * Équivalent Python :
     * <pre>
     *   @router.post("/suggest")
     *   def suggest(request: SuggestRequest, db: Session = Depends(get_db))
     * </pre>
     */
    @PostMapping("/suggest")
    public Object suggest(
            @Valid @RequestBody AIAnalysisRequest req,
            @RequestParam Long storyId) {
        return aiService.analyze(req, storyId);
    }
}