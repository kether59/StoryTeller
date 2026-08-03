package com.kether.storyteller.application.usecase.lore;

import com.kether.storyteller.infrastructure.persistence.jpa.entity.LoreEntry;
import com.kether.storyteller.domain.port.out.persistence.LoreEntryRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FindLoreEntriesByStoryUseCase {

    private final LoreEntryRepositoryPort loreRepo;

    public List<LoreEntry> execute(Long storyId) {
        return loreRepo.findByStoryId(storyId);
    }
}