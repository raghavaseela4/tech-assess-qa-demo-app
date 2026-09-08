package com.example.demo.application.usecases;

import com.example.demo.domain.claim.ClaimStatus;

import java.util.Objects;
import java.util.UUID;

public record UpdateClaimStatusCommand(
    UUID claimId,
    ClaimStatus newStatus,
    UUID adminUserId
) {
    public UpdateClaimStatusCommand {
        Objects.requireNonNull(claimId, "claimId must not be null");
        Objects.requireNonNull(newStatus, "newStatus must not be null");
        Objects.requireNonNull(adminUserId, "adminUserId must not be null");
    }
}
