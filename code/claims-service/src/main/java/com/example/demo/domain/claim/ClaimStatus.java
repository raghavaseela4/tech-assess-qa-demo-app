package com.example.demo.domain.claim;

public enum ClaimStatus {
    SUBMITTED,
    UNDER_REVIEW,
    APPROVED,
    REJECTED,
    CLOSED;

    public boolean canTransitionTo(ClaimStatus target) {
        return switch (this) {
            case SUBMITTED -> target == UNDER_REVIEW || target == REJECTED;
            case UNDER_REVIEW -> target == APPROVED || target == REJECTED || target == SUBMITTED;
            case APPROVED -> target == CLOSED;
            case REJECTED -> target == CLOSED;
            case CLOSED -> false;
        };
    }
}
