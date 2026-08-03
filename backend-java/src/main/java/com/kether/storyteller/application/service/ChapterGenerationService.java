package com.kether.storyteller.application.service;

import com.kether.storyteller.application.dto.ChapterGenerationCommand;
import com.kether.storyteller.application.dto.GeneratedChapterResult;
import com.kether.storyteller.infrastructure.persistence.jpa.entity.Story;
import com.kether.storyteller.infrastructure.persistence.jpa.entity.StoryCharacter;
import com.kether.storyteller.infrastructure.persistence.jpa.entity.StoryLocation;
import com.kether.storyteller.domain.port.in.llm.GenerateChapterUseCase;
import com.kether.storyteller.domain.port.out.llm.LLMGenerationPort;
import com.kether.storyteller.domain.port.out.persistence.*;
import com.kether.storyteller.domain.service.PromptBuilder;
import com.kether.storyteller.domain.service.StyleExtractor;
import com.kether.storyteller.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service d'application : orchestre la génération d'un chapitre.
 *
 * FONCTIONNEMENT :
 * 1. Reçoit une ChapterGenerationCommand (du controller)
 * 2. Récupère l'histoire et ses données liées via les Ports OUT de persistance
 * 3. Filtre les personnages/lieux demandés
 * 4. Extrait le style de référence du dernier manuscrit (StyleExtractor)
 * 5. Construit le prompt (PromptBuilder — domaine)
 * 6. Appelle le LLM (LLMGenerationPort — port OUT)
 * 7. Compte les mots et retourne un GeneratedChapterResult
 *
 * POURQUOI ce n'est pas dans le domaine ?
 * L'orchestration (récupérer des repos, appeler plusieurs domain services,
 * gérer la transaction) est de la logique d'application, pas de logique métier pure.
 *
 * AVANT : Tout était dans LLMService (500+ lignes). Maintenant ce service
 * fait UNE SEULE chose : générer un chapitre. Il fait ~80 lignes.
 */
@Service
@Transactional
public class ChapterGenerationService implements GenerateChapterUseCase {

    private static final Logger log = LoggerFactory.getLogger(ChapterGenerationService.class);

    private final StoryRepositoryPort storyRepo;
    private final CharacterRepositoryPort characterRepo;
    private final LocationRepositoryPort locationRepo;
    private final TimelineEventRepositoryPort timelineRepo;
    private final LoreEntryRepositoryPort loreRepo;
    private final ManuscriptRepositoryPort manuscriptRepo;
    private final LLMGenerationPort llmPort;
    private final PromptBuilder promptBuilder;
    private final StyleExtractor styleExtractor;

    public ChapterGenerationService(StoryRepositoryPort storyRepo,
                                    CharacterRepositoryPort characterRepo,
                                    LocationRepositoryPort locationRepo,
                                    TimelineEventRepositoryPort timelineRepo,
                                    LoreEntryRepositoryPort loreRepo,
                                    ManuscriptRepositoryPort manuscriptRepo,
                                    LLMGenerationPort llmPort,
                                    PromptBuilder promptBuilder,
                                    StyleExtractor styleExtractor) {
        this.storyRepo = storyRepo;
        this.characterRepo = characterRepo;
        this.locationRepo = locationRepo;
        this.timelineRepo = timelineRepo;
        this.loreRepo = loreRepo;
        this.manuscriptRepo = manuscriptRepo;
        this.llmPort = llmPort;
        this.promptBuilder = promptBuilder;
        this.styleExtractor = styleExtractor;
    }

    @Override
    public GeneratedChapterResult generate(ChapterGenerationCommand cmd) {
        log.info("generateChapter — storyId={}, chapterNumber={}, title={}",
                cmd.storyId(), cmd.chapterNumber(), cmd.chapterTitle());

        // 1. Récupération du contexte
        Story story = storyRepo.findById(cmd.storyId())
                .orElseThrow(() -> ResourceNotFoundException.of("Story", cmd.storyId()));

        var allChars = characterRepo.findByStoryId(cmd.storyId());
        var allLocs = locationRepo.findByStoryId(cmd.storyId());
        var timeline = timelineRepo.findByStoryId(cmd.storyId());
        var lore = loreRepo.findByStoryId(cmd.storyId());

        // 2. Filtrage
        var selectedChars = charsFilterByIds(allChars, cmd.includeCharacters());
        var selectedLocs = locationsFilterByIds(allLocs, cmd.includeLocations());

        // 3. Extraction du style de référence
        String styleRef = extractStyleReference(cmd.storyId());

        // 4. Construction des prompts (domaine)
        String systemPrompt = promptBuilder.buildSystemPrompt(
                story, allChars, allLocs, timeline, lore, styleRef);
        String userPrompt = promptBuilder.buildChapterUserPrompt(
                cmd.chapterNumber(), cmd.chapterTitle(), cmd.summary(),
                selectedChars, selectedLocs, resolveTargetWords(cmd.length()));

        // 5. Appel LLM
        try {
            String text = llmPort.generate(systemPrompt, userPrompt, 4000);
            int wordCount = text != null ? text.split("\\s+").length : 0;

            log.info("Generation successful — storyId={}, words={}", cmd.storyId(), wordCount);
            return new GeneratedChapterResult(true, text, cmd.chapterNumber(), cmd.chapterTitle(), wordCount);

        } catch (Exception e) {
            log.error("Error during chapter generation for storyId={}", cmd.storyId(), e);
            throw new RuntimeException("Erreur lors de la génération : " + e.getMessage(), e);
        }
    }

    // --- Méthodes privées d'orchestration ---

    private String extractStyleReference(Long storyId) {
        var manuscripts = manuscriptRepo.findByStoryIdOrderByChapterDesc(storyId);
        if (manuscripts.isEmpty()) return null;

        String lastText = manuscripts.get(0).getText();
        return styleExtractor.extractLastWords(lastText != null ? lastText : "", 3000);
    }

    private <T extends StoryCharacter> List<T>
    charsFilterByIds(List<T> all, List<Long> ids) {
        if (ids == null || ids.isEmpty()) return List.of();
        return all.stream().filter(c -> ids.contains(c.getId())).toList();
    }

    private <T extends StoryLocation> List<T>
    locationsFilterByIds(List<T> all, List<Long> ids) {
        if (ids == null || ids.isEmpty()) return List.of();
        return all.stream().filter(c -> ids.contains(c.getId())).toList();
    }

    private int resolveTargetWords(String length) {
        return switch (length != null ? length.toLowerCase() : "") {
            case "court" -> 800;
            case "long" -> 3000;
            default -> 1500;
        };
    }
}