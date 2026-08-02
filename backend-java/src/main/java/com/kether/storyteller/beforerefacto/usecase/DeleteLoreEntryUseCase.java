package com.kether.storyteller.beforerefacto.usecase;

import com.kether.storyteller.exception.ResourceNotFoundException;
import com.kether.storyteller.repository.LoreEntryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional
public class DeleteLoreEntryUseCase {

    private final LoreEntryRepository loreRepo;

    public void execute(Long loreId) {
        if (!loreRepo.existsById(loreId))
            throw ResourceNotFoundException.of("Lore", loreId);
        loreRepo.deleteById(loreId);
    }
}
