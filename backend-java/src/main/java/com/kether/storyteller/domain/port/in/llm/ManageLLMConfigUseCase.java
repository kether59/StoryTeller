package com.kether.storyteller.domain.port.in.llm;

import com.kether.storyteller.beforerefacto.LLMConfigDto;
import com.kether.storyteller.beforerefacto.LLMTestResultDto;

public interface ManageLLMConfigUseCase {
    LLMConfigDto getCurrentConfig();
    LLMConfigDto updateConfig(LLMConfigDto config);
    LLMTestResultDto testConnection(String provider, String model, String apiKey);
}