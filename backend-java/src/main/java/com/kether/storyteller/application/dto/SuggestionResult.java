package com.kether.storyteller.application.dto;

import java.util.List;
import java.util.Map;

public record SuggestionResult(
        List<Map<String, Object>> suggestions
) {}