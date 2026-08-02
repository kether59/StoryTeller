package com.kether.storyteller.application.dto;

public record RewriteResult(
        boolean success,
        String originalText,
        String rewrittenText,
        String instruction
) {}