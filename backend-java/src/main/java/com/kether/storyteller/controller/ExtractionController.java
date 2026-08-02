package com.kether.storyteller.controller;

import com.kether.storyteller.beforerefacto.ValidationRequest;
import com.kether.storyteller.beforerefacto.ValidationResult;
import com.kether.storyteller.beforerefacto.usecase.ValidateAndCreateUseCase;
import com.kether.storyteller.beforerefacto.usecase.ia.ExtractCharactersUseCase;
import com.kether.storyteller.beforerefacto.usecase.ia.ExtractLocationsUseCase;
import com.kether.storyteller.beforerefacto.usecase.ia.ExtractLoreUseCase;
import com.kether.storyteller.beforerefacto.usecase.ia.ExtractTimelineUseCase;
import com.kether.storyteller.domain.model.ExtractedCharacter;
import com.kether.storyteller.domain.model.ExtractedLocation;
import com.kether.storyteller.domain.model.ExtractedLore;
import com.kether.storyteller.domain.model.ExtractedTimelineEvent;
import com.kether.storyteller.domain.port.out.persistence.ManuscriptRepositoryPort;
import com.kether.storyteller.infrastructure.web.rest.dto.Requests;  // ✅
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/extraction")
@RequiredArgsConstructor
public class ExtractionController {

    private final ExtractCharactersUseCase extractCharactersUseCase;
    private final ExtractLocationsUseCase extractLocationsUseCase;
    private final ExtractTimelineUseCase extractTimelineUseCase;
    private final ExtractLoreUseCase extractLoreUseCase;
    private final ValidateAndCreateUseCase validateAndCreateUseCase;
    private final ManuscriptRepositoryPort manuscriptRepo;

    @PostMapping("/analyze")
    public Requests.ExtractionResponse analyze(@Valid @RequestBody Requests.ExtractionRequest request) {  // ✅
        Long manuscriptId = request.manuscriptId();
        List<String> types = request.extractTypes();

        var manuscript = manuscriptRepo.findById(manuscriptId)
                .orElseThrow(() -> new RuntimeException("Manuscript not found: " + manuscriptId));
        Long storyId = manuscript.getStory().getId();
        String text = manuscript.getText();

        List<ExtractedCharacter> characters = types.contains("characters")
                ? extractCharactersUseCase.execute(storyId, text)
                : List.of();

        List<ExtractedLocation> locations = types.contains("locations")
                ? extractLocationsUseCase.execute(storyId, text)
                : List.of();

        List<ExtractedTimelineEvent> timeline = types.contains("timeline")
                ? extractTimelineUseCase.execute(storyId, text)
                : List.of();

        List<ExtractedLore> lore = types.contains("lore")
                ? extractLoreUseCase.execute(storyId, text)
                : List.of();

        return new Requests.ExtractionResponse(characters, locations, timeline, lore, "Extraction complétée");  // ✅
    }

    @PostMapping("/validate-and-create")
    public ValidationResult validateAndCreate(@Valid @RequestBody ValidationRequest request) {
        return validateAndCreateUseCase.execute(request);
    }
}