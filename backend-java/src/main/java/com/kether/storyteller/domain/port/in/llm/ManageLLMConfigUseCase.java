package com.kether.storyteller.domain.port.in.llm;

import com.kether.storyteller.application.dto.LLMConfigDto;
import com.kether.storyteller.application.dto.LLMTestResultDto;

public interface ManageLLMConfigUseCase {
    LLMConfigDto getCurrentConfig();
    LLMConfigDto updateConfig(LLMConfigDto config);
    LLMTestResultDto testConnection(String provider, String model, String apiKey);
}