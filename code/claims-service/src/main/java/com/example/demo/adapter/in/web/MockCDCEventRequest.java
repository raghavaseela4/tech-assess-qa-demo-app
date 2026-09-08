package com.example.demo.adapter.in.web;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Request body for the test injection API.
 * Used to trigger mock CDC events in Kafka for deterministic E2E testing.
 */
@Schema(description = "Request to trigger a mock CDC event for testing")
public record MockCDCEventRequest(

    @Schema(description = "Claim UUID", example = "123e4567-e89b-12d3-a456-426614174000")
    @NotBlank(message = "Claim ID is required")
    String claimId,

    @Schema(description = "New claim status", example = "APPROVED", allowableValues = {"SUBMITTED", "APPROVED", "REJECTED", "PENDING"})
    @NotBlank(message = "Status is required")
    @Pattern(regexp = "SUBMITTED|APPROVED|REJECTED|PENDING", message = "Invalid status")
    String newStatus
) {}
