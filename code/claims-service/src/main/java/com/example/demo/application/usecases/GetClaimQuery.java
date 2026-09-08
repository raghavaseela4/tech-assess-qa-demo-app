package com.example.demo.application.usecases;

import java.util.UUID;

/**
 * Immutable query object for retrieving a claim.
 */
public record GetClaimQuery(UUID claimId, UUID requestingUserId) {
    public GetClaimQuery {
        java.util.Objects.requireNonNull(claimId, "claimId cannot be null");
        java.util.Objects.requireNonNull(requestingUserId, "requestingUserId cannot be null");
    }
}
