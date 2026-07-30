package com.kether.storyteller.service;

import com.kether.storyteller.dto.request.Requests.*;
import com.kether.storyteller.dto.response.Responses.*;
import com.kether.storyteller.exception.ResourceNotFoundException;
import com.kether.storyteller.entity.LoreEntry;
import com.kether.storyteller.entity.Story;
import com.kether.storyteller.repository.LoreEntryRepository;
import com.kether.storyteller.repository.StoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// ══════════════════════════════════════════════════════════════════════
//  StoryService   – équivalent stories.py
// ══════════════════════════════════════════════════════════════════════
@Service
@Transactional

public class LoreService {

    private final LoreEntryRepository loreRepo;
    private final StoryRepository storyRepo;

    LoreService(LoreEntryRepository loreRepo, StoryRepository storyRepo) {
        this.loreRepo  = loreRepo;
        this.storyRepo = storyRepo;
    }

    public List<LoreEntryResponse> findByStory(Long storyId) {
        return loreRepo.findByStoryIdOrderByTitleAsc(storyId)
                .stream().map(LoreEntryResponse::from).toList();
    }

    public LoreEntryResponse findById(Long id) {
        return LoreEntryResponse.from(
            loreRepo.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Lore", id)));
    }

    public LoreEntryResponse create(LoreEntryCreate req) {
        Story story = storyRepo.findById(req.storyId())
            .orElseThrow(() -> ResourceNotFoundException.of("Histoire", req.storyId()));
        var e = new LoreEntry();
        e.setStory(story);
        e.setTitle(req.title());
        e.setCategory(req.category());
        e.setContent(req.content());
        return LoreEntryResponse.from(loreRepo.save(e));
    }

    public LoreEntryResponse update(Long id, LoreEntryUpdate req) {
        var e = loreRepo.findById(id)
            .orElseThrow(() -> ResourceNotFoundException.of("Lore", id));
        if (req.title()    != null) e.setTitle(req.title());
        if (req.category() != null) e.setCategory(req.category());
        if (req.content()  != null) e.setContent(req.content());
        return LoreEntryResponse.from(loreRepo.save(e));
    }

    public void delete(Long id) {
        if (!loreRepo.existsById(id)) throw ResourceNotFoundException.of("Lore", id);
        loreRepo.deleteById(id);
    }
}