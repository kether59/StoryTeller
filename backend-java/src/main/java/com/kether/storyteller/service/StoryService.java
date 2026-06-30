package com.kether.storyteller.service;

import com.kether.storyteller.dto.request.Requests.*;
import com.kether.storyteller.dto.response.Responses.*;
import com.kether.storyteller.entity.*;
import com.kether.storyteller.exception.ResourceNotFoundException;
import com.kether.storyteller.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// ══════════════════════════════════════════════════════════════════════
//  StoryService   – équivalent stories.py
// ══════════════════════════════════════════════════════════════════════
@Service
@Transactional

public class StoryService {

    private final StoryRepository repo;
    StoryService(StoryRepository repo) { this.repo = repo; }

    public List<StoryResponse> findAll() {
        return repo.findAll().stream().map(StoryResponse::from).toList();
    }

    public StoryResponse findById(Long id) {
        return StoryResponse.from(
            repo.findById(id).orElseThrow(() -> ResourceNotFoundException.of("Histoire", id)));
    }

    public StoryResponse create(StoryCreate req) {
        var story = new Story(req.title(), req.synopsis(), req.blurb());
        return StoryResponse.from(repo.save(story));
    }

    public StoryResponse update(Long id, StoryUpdate req) {
        var story = repo.findById(id)
            .orElseThrow(() -> ResourceNotFoundException.of("Histoire", id));
        if (req.title()    != null) story.setTitle(req.title());
        if (req.synopsis() != null) story.setSynopsis(req.synopsis());
        if (req.blurb()    != null) story.setBlurb(req.blurb());
        return StoryResponse.from(repo.save(story));
    }

    public void delete(Long id) {
        if (!repo.existsById(id)) throw ResourceNotFoundException.of("Histoire", id);
        repo.deleteById(id);
    }
}