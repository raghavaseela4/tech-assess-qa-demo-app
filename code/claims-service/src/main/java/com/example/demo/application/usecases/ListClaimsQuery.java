package com.example.demo.application.usecases;

import java.util.UUID;

/**
 * Immutable query object for listing claims.
 */
public record ListClaimsQuery(UUID userId) {
    public ListClaimsQuery {
        java.util.Objects.requireNonNull(userId, "userId cannot be null");
    }
}
