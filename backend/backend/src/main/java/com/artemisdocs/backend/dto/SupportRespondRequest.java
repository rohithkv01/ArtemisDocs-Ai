package com.artemisdocs.backend.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for a human agent responding to a support ticket.
 */
public record SupportRespondRequest(
        @NotBlank(message = "Response text is required")
        String response
) {}
