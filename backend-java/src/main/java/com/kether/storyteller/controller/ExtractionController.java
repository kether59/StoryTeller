package com.kether.storyteller.controller;

import com.kether.storyteller.application.dto.ExtractionRequest;
import com.kether.storyteller.application.dto.ExtractionResponse;
import com.kether.storyteller.application.dto.ValidationRequest;
import com.kether.storyteller.application.dto.ValidationResult;
import com.kether.storyteller.application.usecase.*;
import com.kether.storyteller.domain.model.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Contrôleur d'extraction – couche Interface.
 * Valide les requêtes HTTP et orchestre les Use Cases.
 * 
 * ✅ Ne connaît QUE les DTOs et Use Cases
 * ❌ N'a AUCUNE logique métier
 */
@RestController
@RequestMapping("/api/extraction")
@RequiredArgsConstructor
public class ExtractionController {

    private final ExtractCharactersUseCase extractCharactersUseCase;
    private final ExtractLocationsUseCase extractLocationsUseCase;
    private final ExtractTimelineUseCase extractTimelineUseCase;
    private final ExtractLoreUseCase extractLoreUseCase;
    private final ValidateAndCreateUseCase validateAndCreateUseCase;

    /**
     * POST /api/extraction/analyze
     * Extrait tous les types demandés d'un manuscrit.
     */
    @PostMapping("/analyze")
    public ExtractionResponse analyze(@Valid @RequestBody ExtractionRequest request) {
        Long manuscriptId = request.manuscriptId();
        List<String> types = request.extractTypes();

        List<ExtractedCharacter> characters = types.contains("characters")
                ? extractCharactersUseCase.execute(manuscriptId)
                : List.of();

        List<ExtractedLocation> locations = types.contains("locations")
                ? extractLocationsUseCase.execute(manuscriptId)
                : List.of();

        List<ExtractedTimelineEvent> timeline = types.contains("timeline")
                ? extractTimelineUseCase.execute(manuscriptId)
                : List.of();

        List<ExtractedLore> lore = types.contains("lore")
                ? extractLoreUseCase.execute(manuscriptId)
                : List.of();

        return new ExtractionResponse(characters, locations, timeline, lore, "Extraction complétée");
    }

    /**
     * POST /api/extraction/validate-and-create
     * Valide et crée un élément extrait après approbation utilisateur.
     */
    @PostMapping("/validate-and-create")
    public ValidationResult validateAndCreate(@Valid @RequestBody ValidationRequest request) {
        return validateAndCreateUseCase.execute(request);
    }
}
