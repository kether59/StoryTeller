package com.kether.storyteller.application.usecase;

import com.kether.storyteller.dto.response.Responses.LoreEntryResponse;
import com.kether.storyteller.repository.LoreEntryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FindLoreEntriesByStoryUseCase {

    private final LoreEntryRepository loreRepo;

    public List<LoreEntryResponse> execute(Long storyId) {
        return loreRepo.findByStoryId(storyId)
            .stream()
            .map(LoreEntryResponse::from)
            .toList();
    }
}
