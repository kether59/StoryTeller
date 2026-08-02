package com.kether.storyteller.application.service;

import com.kether.storyteller.application.dto.ContinuationCommand;
import com.kether.storyteller.application.dto.ContinuationResult;
import com.kether.storyteller.domain.port.in.llm.ContinueWritingUseCase;
import com.kether.storyteller.domain.port.out.llm.LLMGenerationPort;
import com.kether.storyteller.domain.port.out.persistence.*;
import com.kether.storyteller.domain.service.PromptBuilder;
import com.kether.storyteller.domain.service.StyleExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ContinuationService implements ContinueWritingUseCase {

    private static final Logger log = LoggerFactory.getLogger(ContinuationService.class);

    private final ManuscriptRepositoryPort manuscriptRepo;
    private final CharacterRepositoryPort characterRepo;
    private final LocationRepositoryPort locationRepo;
    private final TimelineEventRepositoryPort timelineRepo;
    private final LoreEntryRepositoryPort loreRepo;
    private final LLMGenerationPort llmPort;
    private final PromptBuilder promptBuilder;
    private final StyleExtractor styleExtractor;

    public ContinuationService(ManuscriptRepositoryPort manuscriptRepo,
                               CharacterRepositoryPort characterRepo,
                               LocationRepositoryPort locationRepo,
                               TimelineEventRepositoryPort timelineRepo,
                               LoreEntryRepositoryPort loreRepo,
                               LLMGenerationPort llmPort,
                               PromptBuilder promptBuilder,
                               StyleExtractor styleExtractor) {
        this.manuscriptRepo = manuscriptRepo;
        this.characterRepo = characterRepo;
        this.locationRepo = locationRepo;
        this.timelineRepo = timelineRepo;
        this.loreRepo = loreRepo;
        this.llmPort = llmPort;
        this.promptBuilder = promptBuilder;
        this.styleExtractor = styleExtractor;
    }

    @Override
    public ContinuationResult continueWriting(ContinuationCommand cmd) {
        log.info("continueWriting — manuscriptId={}, length={}", cmd.manuscriptId(), cmd.length());

        var manuscript = manuscriptRepo.findById(cmd.manuscriptId())
                .orElseThrow(() -> new RuntimeException("Manuscrit introuvable"));

        var story = manuscript.getStory();
        var chars = characterRepo.findByStoryId(story.getId());
        var locs = locationRepo.findByStoryId(story.getId());
        var timeline = timelineRepo.findByStoryId(story.getId());
        var lore = loreRepo.findByStoryId(story.getId());

        String styleRef = styleExtractor.extractLastWords(
                manuscript.getText() != null ? manuscript.getText() : "", 3000);

        String systemPrompt = promptBuilder.buildSystemPrompt(story, chars, locs, timeline, lore, styleRef);
        String userPrompt = promptBuilder.buildContinuationUserPrompt(
                manuscript.getTitle(), cmd.direction(), cmd.length() != null ? cmd.length() : 500);

        try {
            String continuation = llmPort.generate(systemPrompt, userPrompt, 2000);
            int words = continuation != null ? continuation.split("\\s+").length : 0;
            log.info("Continuation successful — manuscriptId={}, words={}", cmd.manuscriptId(), words);
            return new ContinuationResult(true, continuation, words);
        } catch (Exception e) {
            log.error("Error during continuation", e);
            throw new RuntimeException("Erreur lors de la continuation : " + e.getMessage(), e);
        }
    }
}