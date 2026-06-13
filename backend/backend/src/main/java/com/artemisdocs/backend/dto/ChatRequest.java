package com.artemisdocs.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for asking a question about a document.
 */
public record ChatRequest(
        @NotBlank(message = "Session ID is required")
        String sessionId,

        @NotNull(message = "Document ID is required")
        Long documentId,

        @NotBlank(message = "Question is required")
        String question
) {}
