package com.kether.storyteller.beforerefacto.usecase;

import com.kether.storyteller.infrastructure.web.rest.dto.Requests.LoreEntryUpdate;
import com.kether.storyteller.infrastructure.web.rest.dto.Responses.LoreEntryResponse;
import com.kether.storyteller.exception.ResourceNotFoundException;
import com.kether.storyteller.repository.LoreEntryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional
public class UpdateLoreEntryUseCase {

    private final LoreEntryRepository loreRepo;

    public LoreEntryResponse execute(Long loreId, LoreEntryUpdate request) {
        var lore = loreRepo.findById(loreId)
            .orElseThrow(() -> ResourceNotFoundException.of("Lore", loreId));

        if (request.title() != null) lore.setTitle(request.title());
        if (request.category() != null) lore.setCategory(request.category());
        if (request.content() != null) lore.setContent(request.content());

        return LoreEntryResponse.from(loreRepo.save(lore));
    }
}
