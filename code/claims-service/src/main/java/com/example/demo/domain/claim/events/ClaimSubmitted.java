package com.example.demo.domain.claim.events;

import com.example.demo.domain.DomainEvent;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public record ClaimSubmitted(
    String eventId,
    UUID claimId,
    UUID userId,
    LocalDate incidentDate,
    BigDecimal claimAmount,
    Instant occurredAt
) implements DomainEvent {

    public ClaimSubmitted {
        Objects.requireNonNull(eventId, "eventId required");
        Objects.requireNonNull(claimId, "claimId required");
        Objects.requireNonNull(userId, "userId required");
        Objects.requireNonNull(incidentDate, "incidentDate required");
        Objects.requireNonNull(claimAmount, "claimAmount required");
        Objects.requireNonNull(occurredAt, "occurredAt required");
    }
}
