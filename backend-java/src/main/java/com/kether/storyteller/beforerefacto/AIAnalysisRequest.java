package com.kether.storyteller.beforerefacto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AIAnalysisRequest(
        String intent,
        @JsonProperty("manuscript_id")
        Long manuscriptId
) {}