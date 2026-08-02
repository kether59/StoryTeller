package com.kether.storyteller.beforerefacto.usecase.lore;

import com.kether.storyteller.domain.entity.LoreEntry;
import com.kether.storyteller.domain.entity.Story;
import com.kether.storyteller.domain.port.out.persistence.LoreEntryRepositoryPort;
import com.kether.storyteller.domain.port.out.persistence.StoryRepositoryPort;
import com.kether.storyteller.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CreateLoreEntryUseCase {

    private final StoryRepositoryPort storyRepo;
    private final LoreEntryRepositoryPort loreRepo;

    public LoreEntry execute(Long storyId, String title, String category, String content) {
        Story story = storyRepo.findById(storyId)
                .orElseThrow(() -> ResourceNotFoundException.of("Story", storyId));

        LoreEntry entry = new LoreEntry();
        entry.setStory(story);
        entry.setTitle(title);
        entry.setCategory(category);
        entry.setContent(content);
        return loreRepo.save(entry);
    }
}