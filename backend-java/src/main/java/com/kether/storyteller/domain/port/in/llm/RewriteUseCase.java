package com.kether.storyteller.domain.port.in.llm;

import com.kether.storyteller.application.dto.RewriteCommand;
import com.kether.storyteller.application.dto.RewriteResult;

public interface RewriteUseCase {
    RewriteResult rewrite(RewriteCommand command);
}