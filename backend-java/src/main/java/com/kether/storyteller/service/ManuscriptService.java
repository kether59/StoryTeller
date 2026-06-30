package com.kether.storyteller.service;

import com.kether.storyteller.dto.request.Requests.*;
import com.kether.storyteller.dto.response.Responses.*;
import com.kether.storyteller.entity.*;
import com.kether.storyteller.exception.ResourceNotFoundException;
import com.kether.storyteller.repository.*;
import com.kether.storyteller.service.llm.NLPService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.Map;

// ══════════════════════════════════════════════════════════════════════
//  StoryService   – équivalent stories.py
// ══════════════════════════════════════════════════════════════════════
@Service
@Transactional

public class ManuscriptService {

    private final ManuscriptRepository manuscriptRepo;
    private final StoryRepository      storyRepo;
    private final CharacterRepository  characterRepo;
    private final NLPService nlpService;

    ManuscriptService(ManuscriptRepository manuscriptRepo, StoryRepository storyRepo,
                      CharacterRepository characterRepo, NLPService nlpService) {
        this.manuscriptRepo = manuscriptRepo;
        this.storyRepo      = storyRepo;
        this.characterRepo  = characterRepo;
        this.nlpService     = nlpService;
    }

    public List<ManuscriptResponse> findByStory(Long storyId) {
        return manuscriptRepo.findByStoryIdOrderByChapterAsc(storyId)
                .stream().map(ManuscriptResponse::from).toList();
    }

    public ManuscriptResponse findById(Long id) {
        return ManuscriptResponse.from(
            manuscriptRepo.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Manuscrit", id)));
    }

    public ManuscriptResponse create(ManuscriptCreate req) {
        Story story = storyRepo.findById(req.storyId())
            .orElseThrow(() -> ResourceNotFoundException.of("Histoire", req.storyId()));
        var m = new Manuscript();
        m.setStory(story);
        m.setTitle(req.title());
        m.setChapter(req.chapter() != null ? req.chapter() : 1);
        m.setText(req.text());
        m.setStatus(req.status());
        return ManuscriptResponse.from(manuscriptRepo.save(m));
    }

    public ManuscriptResponse update(Long id, ManuscriptUpdate req) {
        var m = manuscriptRepo.findById(id)
            .orElseThrow(() -> ResourceNotFoundException.of("Manuscrit", id));
        if (req.title()   != null) m.setTitle(req.title());
        if (req.chapter() != null) m.setChapter(req.chapter());
        if (req.text()    != null) m.setText(req.text());
        if (req.status()  != null) m.setStatus(req.status());
        return ManuscriptResponse.from(manuscriptRepo.save(m));
    }

    public void delete(Long id) {
        if (!manuscriptRepo.existsById(id)) throw ResourceNotFoundException.of("Manuscrit", id);
        manuscriptRepo.deleteById(id);
    }

    /** Analyse NLP d'un manuscrit – équivalent analyze_manuscript Python (manuscript.py). */
    @Transactional(readOnly = true)
    public ManuscriptAnalysis analyze(Long id, String mode) {
        Manuscript m = manuscriptRepo.findById(id)
            .orElseThrow(() -> ResourceNotFoundException.of("Manuscrit", id));

        String text = m.getText() != null ? m.getText() : "";
        NLPService.NLPResult nlpResult = nlpService.process(text);

        // Mentions de personnages (string matching)
        var allChars   = characterRepo.findByStoryId(m.getStory().getId());
        var charNames  = allChars.stream().map(StoryCharacter::getName)
                                 .filter(n -> n != null && !n.isBlank()).toList();
        var mentions   = nlpService.findMentions(text, charNames);

        var summary = new ArrayList<Map<String, Object>>();
        if (!mentions.isEmpty()) {
            summary.add(java.util.Map.of("type", "mentions",
                    "count", mentions.size(), "items", mentions));
        }

        return new ManuscriptAnalysis(
            m.getId(), m.getTitle(), m.getChapter(),
            mode, m.getStatus(),
            summary,
            nlpResult.entities(),
            text.length()
        );
    }
}