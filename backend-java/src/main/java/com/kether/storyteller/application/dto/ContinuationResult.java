package com.kether.storyteller.application.dto;

public record ContinuationResult
        (String continuation,
         String summary,
         String suggestion
        )
{
}
