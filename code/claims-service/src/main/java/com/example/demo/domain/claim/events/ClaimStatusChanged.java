package com.example.demo.domain.claim.events;

import com.example.demo.domain.DomainEvent;
import com.example.demo.domain.claim.ClaimStatus;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record ClaimStatusChanged(
    String eventId,
    UUID claimId,
    ClaimStatus oldStatus,
    ClaimStatus newStatus,
    UUID changedBy,
    Instant occurredAt
) implements DomainEvent {

    public ClaimStatusChanged {
        Objects.requireNonNull(eventId, "eventId required");
        Objects.requireNonNull(claimId, "claimId required");
        Objects.requireNonNull(oldStatus, "oldStatus required");
        Objects.requireNonNull(newStatus, "newStatus required");
        Objects.requireNonNull(occurredAt, "occurredAt required");
    }
}
