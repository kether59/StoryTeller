package com.kether.storyteller.domain.model;

import java.util.List;

public record ExtractedTimelineEvent(
        String title,
        String date,
        String summary,
        Integer sortOrder,
        List<String> characterNames,
        String locationName,
        double confidence
) {
    public ExtractedTimelineEvent {
        if (title == null || title.isBlank())
            throw new IllegalArgumentException("Un événement doit avoir un titre");
        if (confidence < 0 || confidence > 1)
            throw new IllegalArgumentException("Confidence invalide");
    }
}
