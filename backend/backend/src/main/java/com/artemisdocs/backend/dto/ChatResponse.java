package com.artemisdocs.backend.dto;

import java.util.List;

/**
 * Response DTO for AI-generated answers.
 * Includes confidence score and escalation status.
 */
public record ChatResponse(
        String sessionId,
        String answer,
        Double confidenceScore,
        boolean isEscalated,
        List<String> sources
) {}
