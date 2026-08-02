package com.kether.storyteller.beforerefacto.usecase.ia;

import com.kether.storyteller.domain.entity.Story;
import com.kether.storyteller.domain.model.ExtractedCharacter;
import com.kether.storyteller.domain.port.out.llm.LLMGenerationPort;
import com.kether.storyteller.domain.port.out.persistence.StoryRepositoryPort;
import com.kether.storyteller.exception.ResourceNotFoundException;
import com.kether.storyteller.infrastructure.llm.parser.JacksonCharacterExtractionParser;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExtractCharactersUseCase {

    private static final Logger log = LoggerFactory.getLogger(ExtractCharactersUseCase.class);

    private final StoryRepositoryPort storyRepo;
    private final LLMGenerationPort llmPort;
    private final JacksonCharacterExtractionParser parser;

    public List<ExtractedCharacter> execute(Long storyId, String text) {
        Story story = storyRepo.findById(storyId)
                .orElseThrow(() -> ResourceNotFoundException.of("Story", storyId));

        String prompt = buildExtractionPrompt(story, text);
        String raw = llmPort.generate("Tu es un extracteur de personnages littéraires.", prompt, 3000);
        return parser.parse(raw);
    }

    private String buildExtractionPrompt(Story story, String text) {
        return """
            Analyse ce texte et extrais les personnages au format JSON.
            Histoire : %s
            Texte : %s
            Retourne un JSON avec une clé "characters" contenant une liste d'objets :
            { "name": "...", "surname": "...", "role": "...", "age": 25, "physicalDescription": "...", "personality": "...", "motivation": "..." }
            """.formatted(story.getTitle(), text);
    }
}