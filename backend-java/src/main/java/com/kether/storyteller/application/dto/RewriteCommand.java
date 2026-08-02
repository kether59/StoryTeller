package com.kether.storyteller.application.dto;

public record RewriteCommand (
    String storyId,
    String content,
    String userId
)
{
}
