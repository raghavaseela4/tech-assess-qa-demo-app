package com.example.demo.application.usecases;

import com.example.demo.domain.claim.ClaimStatus;

import java.util.Optional;
import java.util.UUID;

/**
 * Query to list all claims with optional filters.
 * Used by admin to view claims across all users.
 */
public record ListAllClaimsQuery(
    Optional<ClaimStatus> statusFilter,
    Optional<UUID> userIdFilter
) {

    public ListAllClaimsQuery {
        if (statusFilter == null) {
            statusFilter = Optional.empty();
        }
        if (userIdFilter == null) {
            userIdFilter = Optional.empty();
        }
    }

    public static ListAllClaimsQuery all() {
        return new ListAllClaimsQuery(Optional.empty(), Optional.empty());
    }

    public static ListAllClaimsQuery byStatus(ClaimStatus status) {
        return new ListAllClaimsQuery(Optional.of(status), Optional.empty());
    }

    public static ListAllClaimsQuery byUserId(UUID userId) {
        return new ListAllClaimsQuery(Optional.empty(), Optional.of(userId));
    }

    public static ListAllClaimsQuery byStatusAndUserId(ClaimStatus status, UUID userId) {
        return new ListAllClaimsQuery(Optional.of(status), Optional.of(userId));
    }
}
