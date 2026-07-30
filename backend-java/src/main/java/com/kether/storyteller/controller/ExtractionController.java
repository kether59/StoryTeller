package com.kether.storyteller.controller;

import com.kether.storyteller.application.usecase.ExtractCharactersUseCase;
import com.kether.storyteller.dto.request.Requests.ExtractionRequest;
import com.kether.storyteller.dto.response.Responses.ExtractionResult;
import com.kether.storyteller.dto.response.Responses.ExtractedCharacter;
import com.kether.storyteller.service.ExtractionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/extraction")
@RequiredArgsConstructor
public class ExtractionController {

    private final ExtractionService oldService;                 // ← ANCIEN, intact
    private final ExtractCharactersUseCase newExtractUseCase;   // ← NOUVEAU

    @PostMapping("/analyze")
    public ExtractionResult analyze(@Valid @RequestBody ExtractionRequest req) {

        // 🎯 Bascule progressive : si on demande UNIQUEMENT des personnages
        // et que le flag "v2" n'est pas désactivé, on passe par le nouveau code
        if (isCharactersOnly(req)) {
            var domainCharacters = newExtractUseCase.execute(req.manuscriptId());

            // Mapping domain → DTO response (temporaire, on fera MapStruct après)
            var responseChars = domainCharacters.stream()
                    .map(c -> new ExtractedCharacter(
                            c.name(), c.surname(), c.role(), c.age(),
                            c.physicalDescription(), c.personality(),
                            c.motivation(), c.confidence()
                    ))
                    .toList();

            return new ExtractionResult(
                    responseChars,
                    List.of(),   // locations vides
                    List.of(),   // timeline vide
                    List.of(),   // lore vide
                    "Extraction via nouveau moteur"
            );
        }

        // Fallback : tout le reste passe par l'ancien code
        return oldService.analyze(req);
    }

    private boolean isCharactersOnly(ExtractionRequest req) {
        return req.extractTypes() != null
                && req.extractTypes().size() == 1
                && req.extractTypes().get(0).equals("characters");
    }
}