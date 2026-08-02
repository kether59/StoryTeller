package com.kether.storyteller.beforerefacto.usecase.lore;

import com.kether.storyteller.domain.entity.LoreEntry;
import com.kether.storyteller.domain.port.out.persistence.LoreEntryRepositoryPort;
import com.kether.storyteller.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UpdateLoreEntryUseCase {

    private final LoreEntryRepositoryPort loreRepo;

    public LoreEntry execute(Long id, String title, String category, String content) {
        LoreEntry entry = loreRepo.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("LoreEntry", id));

        if (title != null) entry.setTitle(title);
        if (category != null) entry.setCategory(category);
        if (content != null) entry.setContent(content);

        return loreRepo.save(entry);
    }
}