package com.kether.storyteller.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AIAnalysisRequest(
        String intent,
        @JsonProperty("manuscript_id")
        Long manuscriptId
) {}