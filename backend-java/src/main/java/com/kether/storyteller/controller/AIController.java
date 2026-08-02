package com.kether.storyteller.controller;

import com.kether.storyteller.beforerefacto.AIAnalysisRequest;
import com.kether.storyteller.beforerefacto.usecase.ia.*;
import com.kether.storyteller.domain.port.out.persistence.ManuscriptRepositoryPort;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AIController {

    private final AnalyzeCharacterLinksUseCase analyzeCharacterLinksUseCase;
    private final FindTimelineConflictsUseCase findTimelineConflictsUseCase;
    private final CheckScriptConsistencyUseCase checkScriptConsistencyUseCase;
    private final CheckCharacterBehaviorUseCase checkCharacterBehaviorUseCase;
    private final CheckLoreConsistencyUseCase checkLoreConsistencyUseCase;
    private final ManuscriptRepositoryPort manuscriptRepo;

    @PostMapping("/suggest")
    public Object suggest(
            @Valid @RequestBody AIAnalysisRequest request,
            @RequestParam Long storyId) {

        String manuscriptText = null;
        if (request.manuscriptId() != null) {
            manuscriptText = manuscriptRepo.findById(request.manuscriptId())
                    .map(m -> m.getText())
                    .orElse(null);
        }

        return switch (request.intent()) {
            case "link_characters" ->
                    analyzeCharacterLinksUseCase.execute(storyId);

            case "timeline_conflicts" ->
                    findTimelineConflictsUseCase.execute(storyId);

            case "script_consistency" ->
                    checkScriptConsistencyUseCase.execute(storyId, manuscriptText);

            case "character_behavior" ->
                    checkCharacterBehaviorUseCase.execute(storyId, manuscriptText);

            case "lore_check" ->
                    checkLoreConsistencyUseCase.execute(storyId, manuscriptText);

            default ->
                    throw new IllegalArgumentException("Intent inconnu : " + request.intent());
        };
    }
}