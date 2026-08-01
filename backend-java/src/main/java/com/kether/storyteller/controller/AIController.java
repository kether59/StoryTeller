package com.kether.storyteller.controller;

import com.kether.storyteller.application.dto.AIAnalysisRequest;
import com.kether.storyteller.application.usecase.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * Contrôleur IA – couche Interface.
 * Valide les requêtes HTTP et orchestre les Use Cases d'analyse IA.
 * 
 * ✅ Ne connaît QUE les DTOs et Use Cases
 * ❌ N'a AUCUNE logique métier
 */
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AIController {

    private final AnalyzeCharacterLinksUseCase analyzeCharacterLinksUseCase;
    private final FindTimelineConflictsUseCase findTimelineConflictsUseCase;
    private final CheckScriptConsistencyUseCase checkScriptConsistencyUseCase;
    private final CheckCharacterBehaviorUseCase checkCharacterBehaviorUseCase;
    private final CheckLoreConsistencyUseCase checkLoreConsistencyUseCase;

    /**
     * POST /api/ai/suggest
     * 
     * Intents supportés :
     * - link_characters        → Analyser les liens entre personnages
     * - timeline_conflicts     → Trouver les conflits de chronologie
     * - script_consistency     → Vérifier la cohérence du script
     * - character_behavior     → Analyser la cohérence comportementale
     * - lore_check             → Vérifier le lore
     */
    @PostMapping("/suggest")
    public Object suggest(
            @Valid @RequestBody AIAnalysisRequest request,
            @RequestParam Long storyId) {
        
        String manuscriptText = null;  // On peut le passer optionnellement
        
        return switch (request.intent()) {
            case "link_characters" -> 
                analyzeCharacterLinksUseCase.execute(storyId, manuscriptText);
            
            case "timeline_conflicts" -> 
                findTimelineConflictsUseCase.execute(storyId);
            
            case "script_consistency" -> 
                checkScriptConsistencyUseCase.execute(storyId, request.manuscriptId());
            
            case "character_behavior" -> 
                checkCharacterBehaviorUseCase.execute(storyId, request.manuscriptId());
            
            case "lore_check" -> 
                checkLoreConsistencyUseCase.execute(storyId, request.manuscriptId());
            
            default -> 
                throw new IllegalArgumentException("Intent inconnu : " + request.intent());
        };
    }
}
