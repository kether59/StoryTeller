package com.kether.storyteller.interfaces.rest.controller;

import com.kether.storyteller.application.dto.ValidationResult;
import com.kether.storyteller.application.usecase.ValidateAndCreateUseCase;
import com.kether.storyteller.application.usecase.extraction.ExtractCharactersUseCase;
import com.kether.storyteller.application.usecase.extraction.ExtractLocationsUseCase;
import com.kether.storyteller.application.usecase.extraction.ExtractLoreUseCase;
import com.kether.storyteller.application.usecase.extraction.ExtractTimelineUseCase;
import com.kether.storyteller.domain.model.ExtractedCharacter;
import com.kether.storyteller.domain.model.ExtractedLocation;
import com.kether.storyteller.domain.model.ExtractedLore;
import com.kether.storyteller.domain.model.ExtractedTimelineEvent;
import com.kether.storyteller.domain.port.out.persistence.ManuscriptRepositoryPort;
import com.kether.storyteller.infrastructure.web.rest.dto.Requests;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.function.Supplier;

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
    public Requests.ExtractionResponse analyze(@Valid @RequestBody Requests.ExtractionRequest request) {
        Long manuscriptId = request.manuscriptId();
        List<String> types = request.extractTypes();

        var manuscript = manuscriptRepo.findById(manuscriptId)
                .orElseThrow(() -> new RuntimeException("Manuscript not found: " + manuscriptId));
        Long storyId = manuscript.getStory().getId();
        String text = manuscript.getText();

        List<ExtractedCharacter> characters = extractIfRequested("characters", types,
                () -> extractCharactersUseCase.execute(storyId, text));

        List<ExtractedLocation> locations = extractIfRequested("locations", types,
                () -> extractLocationsUseCase.execute(storyId, text));

        List<ExtractedTimelineEvent> timeline = extractIfRequested("timeline", types,
                () -> extractTimelineUseCase.execute(storyId, text));


        List<ExtractedLore> lore = extractIfRequested("lore", types,
                () -> extractLoreUseCase.execute(storyId, text));

        return new Requests.ExtractionResponse(characters, locations, timeline, lore, "Extraction complétée");
    }

    private <T> List<T> extractIfRequested(String type, List<String> types,
                                           Supplier<List<T>> supplier) {
        return types.contains(type) ? supplier.get() : List.of();
    }

    @PostMapping("/validate-and-create")
    public ValidationResult validateAndCreate(@Valid @RequestBody Requests.ValidationRequest request) {
        return validateAndCreateUseCase.execute(request);
    }
}