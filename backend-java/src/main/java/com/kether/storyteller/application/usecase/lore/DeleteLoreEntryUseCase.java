package com.kether.storyteller.application.usecase.lore;

import com.kether.storyteller.domain.port.out.persistence.LoreEntryRepositoryPort;
import com.kether.storyteller.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeleteLoreEntryUseCase {

    private final LoreEntryRepositoryPort loreRepo;

    public void execute(Long id) {
        if (loreRepo.findById(id).isEmpty()) {
            throw ResourceNotFoundException.of("LoreEntry", id);
        }
        loreRepo.deleteById(id);
    }
}