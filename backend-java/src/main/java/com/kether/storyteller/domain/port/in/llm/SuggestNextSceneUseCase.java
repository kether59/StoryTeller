package com.kether.storyteller.domain.port.in.llm;

import com.kether.storyteller.application.dto.SuggestionCommand;
import com.kether.storyteller.application.dto.SuggestionResult;

public interface SuggestNextSceneUseCase {
    SuggestionResult suggest(SuggestionCommand command);
}