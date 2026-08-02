package com.kether.storyteller.application.dto;

public record RewriteCommand(
        String text,
        String instruction
) {}