package com.example.demo.application.auth;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Port interface for Claims Service operations.
 * Adapter implementations handle communication with Claims Service REST API.
 */
public interface ClaimsServicePort {

    UserRecord createUser(UUID userId, String email, String name, String role);

    UserRecord getUser(UUID userId);

    ClaimRecord submitClaim(String userJwtToken, LocalDate incidentDate, String incidentLocation,
                            String description, BigDecimal claimAmount);

    List<ClaimSummaryRecord> listClaims(String userJwtToken, UUID userId);

    ClaimRecord getClaim(String userJwtToken, UUID claimId);

    record UserRecord(UUID userId, String email, String name, String role, OffsetDateTime createdAt) {}

    record ClaimRecord(UUID claimId, UUID userId, LocalDate incidentDate, String incidentLocation,
                       String description, BigDecimal claimAmount, String status,
                       OffsetDateTime createdAt, OffsetDateTime updatedAt) {}

    record ClaimSummaryRecord(UUID claimId, LocalDate incidentDate, String description,
                              BigDecimal claimAmount, String status, OffsetDateTime createdAt) {}
}
