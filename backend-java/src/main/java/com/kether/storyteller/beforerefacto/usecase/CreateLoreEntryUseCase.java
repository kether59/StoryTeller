package com.kether.storyteller.beforerefacto.usecase;

import com.kether.storyteller.infrastructure.web.rest.dto.Requests.LoreEntryCreate;
import com.kether.storyteller.infrastructure.web.rest.dto.Responses.LoreEntryResponse;
import com.kether.storyteller.domain.entity.Story;
import com.kether.storyteller.domain.entity.LoreEntry;
import com.kether.storyteller.exception.ResourceNotFoundException;
import com.kether.storyteller.repository.LoreEntryRepository;
import com.kether.storyteller.repository.StoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional
public class CreateLoreEntryUseCase {

    private final LoreEntryRepository loreRepo;
    private final StoryRepository storyRepo;

    public LoreEntryResponse execute(LoreEntryCreate request) {
        Story story = storyRepo.findById(request.storyId())
            .orElseThrow(() -> ResourceNotFoundException.of("Histoire", request.storyId()));

        var lore = new LoreEntry();
        lore.setStory(story);
        lore.setTitle(request.title());
        lore.setCategory(request.category());
        lore.setContent(request.content());

        return LoreEntryResponse.from(loreRepo.save(lore));
    }
}
