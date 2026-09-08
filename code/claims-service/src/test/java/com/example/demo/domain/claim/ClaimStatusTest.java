package com.example.demo.domain.claim;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Exhaustive test of the {@link ClaimStatus} transition matrix.
 *
 * <p>This is the single source of truth for which status changes are legal.
 * It's tested as a full 5x5 matrix (25 cases) rather than spot-checking a
 * few transitions, because the workflow-of-record for the whole app (what
 * an admin is allowed to do to a claim) hangs entirely on this switch
 * statement, and the UI (claim-status-transitions.ts) duplicates this same
 * table by hand — this test is what keeps that duplication honest.</p>
 */
class ClaimStatusTest {

    @ParameterizedTest(name = "{0} -> {1} allowed = {2}")
    @CsvSource({
            // from,          to,             allowed
            "SUBMITTED,       SUBMITTED,      false",
            "SUBMITTED,       UNDER_REVIEW,   true",
            "SUBMITTED,       APPROVED,       false",
            "SUBMITTED,       REJECTED,       true",
            "SUBMITTED,       CLOSED,         false",

            "UNDER_REVIEW,    SUBMITTED,      true",
            "UNDER_REVIEW,    UNDER_REVIEW,   false",
            "UNDER_REVIEW,    APPROVED,       true",
            "UNDER_REVIEW,    REJECTED,       true",
            "UNDER_REVIEW,    CLOSED,         false",

            "APPROVED,        SUBMITTED,      false",
            "APPROVED,        UNDER_REVIEW,   false",
            "APPROVED,        APPROVED,       false",
            "APPROVED,        REJECTED,       false",
            "APPROVED,        CLOSED,         true",

            "REJECTED,        SUBMITTED,      false",
            "REJECTED,        UNDER_REVIEW,   false",
            "REJECTED,        APPROVED,       false",
            "REJECTED,        REJECTED,       false",
            "REJECTED,        CLOSED,         true",

            "CLOSED,          SUBMITTED,      false",
            "CLOSED,          UNDER_REVIEW,   false",
            "CLOSED,          APPROVED,       false",
            "CLOSED,          REJECTED,       false",
            "CLOSED,          CLOSED,         false",
    })
    void transitionMatrix(ClaimStatus from, ClaimStatus to, boolean expectedAllowed) {
        assertThat(from.canTransitionTo(to)).isEqualTo(expectedAllowed);
    }
}
