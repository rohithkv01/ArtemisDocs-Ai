package com.artemisdocs.backend.dto;

import java.time.LocalDateTime;

/**
 * Response DTO returned after a successful document upload.
 */
public record UploadResponse(
        Long id,
        String filename,
        LocalDateTime uploadDate,
        Integer pageCount,
        Long fileSize,
        String message
) {}
