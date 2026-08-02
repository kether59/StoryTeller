package com.kether.storyteller.application.dto;

public record ContinuationCommand(
        Long manuscriptId,
        String direction,
        Integer length
) {}