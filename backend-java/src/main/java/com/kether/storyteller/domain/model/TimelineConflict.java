package com.kether.storyteller.domain.model;

public record TimelineConflict(
        String type,
        String description,
        String event1,
        String event2,
        String suggestion,
        double severity
) {
    public TimelineConflict {
        if (type == null || type.isBlank())
            throw new IllegalArgumentException("Un conflit doit avoir un type");
        if (severity < 0 || severity > 1)
            throw new IllegalArgumentException("Severité invalide");
    }
}
