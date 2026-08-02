package com.kether.storyteller.domain.port.in.llm;

import com.kether.storyteller.application.dto.ContinuationCommand;
import com.kether.storyteller.application.dto.ContinuationResult;

public interface ContinueWritingUseCase {
    ContinuationResult continueWriting(ContinuationCommand command);
}