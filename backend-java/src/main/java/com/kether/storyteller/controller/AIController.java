package com.kether.storyteller.controller;

import com.kether.storyteller.application.usecase.analysis.*;
import com.kether.storyteller.application.dto.AIAnalysisRequest;
import com.kether.storyteller.domain.port.out.persistence.ManuscriptRepositoryPort;
import com.kether.storyteller.exception.ResourceNotFoundException;
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
    public Object suggest(@Valid @RequestBody AIAnalysisRequest request) {

        Long storyId = resolveStoryId(request.manuscriptId());
        String manuscriptText = resolveManuscriptText(request.manuscriptId());

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

    private Long resolveStoryId(Long manuscriptId) {
        if (manuscriptId == null) {
            throw new IllegalArgumentException("manuscriptId est requis");
        }
        return manuscriptRepo.findById(manuscriptId)
                .map(m -> m.getStory().getId())
                .orElseThrow(() -> ResourceNotFoundException.of("Manuscript", manuscriptId));
    }

    private String resolveManuscriptText(Long manuscriptId) {
        if (manuscriptId == null) return null;
        return manuscriptRepo.findById(manuscriptId)
                .map(m -> m.getText())
                .orElse(null);
    }
}