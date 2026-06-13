package com.artemisdocs.backend.dto;

import java.time.LocalDateTime;

/**
 * Response DTO for support ticket details.
 */
public record SupportTicketResponse(
        Long id,
        String sessionId,
        String question,
        String context,
        String status,
        String response,
        String aiAnswer,
        Double confidenceScore,
        String documentFilename,
        Long documentId,
        LocalDateTime createdAt,
        LocalDateTime resolvedAt
) {}
