package com.example.demo.domain.dashboard;

import com.example.demo.domain.claim.ClaimStatus;

import java.util.Map;
import java.util.Objects;

/**
 * Aggregate statistics for admin dashboard.
 * Immutable value object containing system-wide metrics.
 */
public record DashboardStats(
    int totalUsers,
    int totalClaims,
    Map<ClaimStatus, Integer> claimsByStatus
) {
    public DashboardStats {
        Objects.requireNonNull(claimsByStatus, "claimsByStatus must not be null");

        if (totalUsers < 0) {
            throw new IllegalArgumentException("totalUsers cannot be negative");
        }
        if (totalClaims < 0) {
            throw new IllegalArgumentException("totalClaims cannot be negative");
        }

        claimsByStatus = Map.copyOf(claimsByStatus);
    }
}
