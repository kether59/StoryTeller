package com.kether.storyteller.application.dto;

public record ContinuationCommand (
    String storyId,
    String content,
    String userId
)

{
}
