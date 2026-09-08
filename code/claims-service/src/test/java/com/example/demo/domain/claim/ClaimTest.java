package com.example.demo.domain.claim;

import com.example.demo.domain.claim.events.ClaimStatusChanged;
import com.example.demo.domain.claim.events.ClaimSubmitted;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for the {@link Claim} aggregate root.
 *
 * <p>Priority rationale: Claim is the core domain entity and the only place
 * business rules (amount limits, date bounds, description/location length,
 * status transitions) are enforced. A defect here is silently reachable from
 * every entry point (UI wizard, BFF, direct API, CDC bypass) and would allow
 * invalid claims into the system or an incorrect audit trail. These are pure
 * unit tests with no Spring context, so they are fast and cheap to run on
 * every commit.</p>
 */
class ClaimTest {

    private static final UUID CLAIM_ID = UUID.randomUUID();
    private static final UUID USER_ID = UUID.randomUUID();
    private static final LocalDate VALID_DATE = LocalDate.now().minusDays(1);
    private static final String VALID_LOCATION = "123 Main Street";
    private static final String VALID_DESCRIPTION = "The car was rear-ended at a red light.";
    private static final BigDecimal VALID_AMOUNT = new BigDecimal("500.00");

    private Claim newValidClaim() {
        return new Claim(CLAIM_ID, USER_ID, VALID_DATE, VALID_LOCATION, VALID_DESCRIPTION, VALID_AMOUNT);
    }

    @Nested
    @DisplayName("construction / validation rules")
    class Construction {

        @Test
        @DisplayName("valid input creates a SUBMITTED claim and raises ClaimSubmitted")
        void createsValidClaim() {
            Claim claim = newValidClaim();

            assertThat(claim.getStatus()).isEqualTo(ClaimStatus.SUBMITTED);
            assertThat(claim.getClaimId()).isEqualTo(CLAIM_ID);
            assertThat(claim.getDomainEvents()).hasSize(1);
            assertThat(claim.getDomainEvents().get(0)).isInstanceOf(ClaimSubmitted.class);

            ClaimSubmitted event = (ClaimSubmitted) claim.getDomainEvents().get(0);
            assertThat(event.claimId()).isEqualTo(CLAIM_ID);
            assertThat(event.userId()).isEqualTo(USER_ID);
        }

        @Test
        @DisplayName("trims description and location before storing")
        void trimsWhitespace() {
            Claim claim = new Claim(CLAIM_ID, USER_ID, VALID_DATE,
                    "   " + VALID_LOCATION + "   ", "   " + VALID_DESCRIPTION + "   ", VALID_AMOUNT);

            assertThat(claim.getIncidentLocation()).isEqualTo(VALID_LOCATION);
            assertThat(claim.getDescription()).isEqualTo(VALID_DESCRIPTION);
        }

        @Test
        @DisplayName("rejects a future incident date")
        void rejectsFutureIncidentDate() {
            LocalDate tomorrow = LocalDate.now().plusDays(1);

            assertThatThrownBy(() ->
                    new Claim(CLAIM_ID, USER_ID, tomorrow, VALID_LOCATION, VALID_DESCRIPTION, VALID_AMOUNT))
                    .isInstanceOf(InvalidClaimException.class)
                    .hasMessageContaining("future");
        }

        @Test
        @DisplayName("accepts today's date as a boundary case")
        void acceptsTodayAsIncidentDate() {
            Claim claim = new Claim(CLAIM_ID, USER_ID, LocalDate.now(),
                    VALID_LOCATION, VALID_DESCRIPTION, VALID_AMOUNT);

            assertThat(claim.getIncidentDate()).isEqualTo(LocalDate.now());
        }

        @Test
        @DisplayName("rejects a description under 10 characters")
        void rejectsTooShortDescription() {
            assertThatThrownBy(() ->
                    new Claim(CLAIM_ID, USER_ID, VALID_DATE, VALID_LOCATION, "too short", VALID_AMOUNT))
                    .isInstanceOf(InvalidClaimException.class)
                    .hasMessageContaining("10-1000");
        }

        @Test
        @DisplayName("rejects a description over 1000 characters")
        void rejectsTooLongDescription() {
            String tooLong = "a".repeat(1001);

            assertThatThrownBy(() ->
                    new Claim(CLAIM_ID, USER_ID, VALID_DATE, VALID_LOCATION, tooLong, VALID_AMOUNT))
                    .isInstanceOf(InvalidClaimException.class)
                    .hasMessageContaining("10-1000");
        }

        @Test
        @DisplayName("accepts a description at exactly the 10-character lower boundary")
        void acceptsDescriptionAtLowerBoundary() {
            String exactlyTen = "1234567890";

            Claim claim = new Claim(CLAIM_ID, USER_ID, VALID_DATE, VALID_LOCATION, exactlyTen, VALID_AMOUNT);

            assertThat(claim.getDescription()).isEqualTo(exactlyTen);
        }

        @Test
        @DisplayName("rejects a description that is only whitespace padding around 10 chars")
        void rejectsDescriptionThatIsTooShortAfterTrim() {
            // 9 real characters plus padding trims down to under the minimum —
            // this guards against validating the untrimmed length.
            String padded = "   123456789   ";

            assertThatThrownBy(() ->
                    new Claim(CLAIM_ID, USER_ID, VALID_DATE, VALID_LOCATION, padded, VALID_AMOUNT))
                    .isInstanceOf(InvalidClaimException.class);
        }

        @Test
        @DisplayName("rejects an incident location under 5 characters")
        void rejectsTooShortLocation() {
            assertThatThrownBy(() ->
                    new Claim(CLAIM_ID, USER_ID, VALID_DATE, "abc", VALID_DESCRIPTION, VALID_AMOUNT))
                    .isInstanceOf(InvalidClaimException.class)
                    .hasMessageContaining("5-200");
        }

        @Test
        @DisplayName("rejects an incident location over 200 characters")
        void rejectsTooLongLocation() {
            String tooLong = "a".repeat(201);

            assertThatThrownBy(() ->
                    new Claim(CLAIM_ID, USER_ID, VALID_DATE, tooLong, VALID_DESCRIPTION, VALID_AMOUNT))
                    .isInstanceOf(InvalidClaimException.class)
                    .hasMessageContaining("5-200");
        }

        @ParameterizedTest(name = "rejects non-positive claim amount: {0}")
        @ValueSource(strings = {"0", "-0.01", "-500"})
        @DisplayName("rejects zero or negative claim amounts")
        void rejectsNonPositiveAmount(String amount) {
            assertThatThrownBy(() ->
                    new Claim(CLAIM_ID, USER_ID, VALID_DATE, VALID_LOCATION, VALID_DESCRIPTION,
                            new BigDecimal(amount)))
                    .isInstanceOf(InvalidClaimException.class)
                    .hasMessageContaining("positive");
        }

        @Test
        @DisplayName("accepts the maximum allowed claim amount (boundary, inclusive)")
        void acceptsMaxAmountBoundary() {
            Claim claim = new Claim(CLAIM_ID, USER_ID, VALID_DATE, VALID_LOCATION, VALID_DESCRIPTION,
                    Claim.MAX_CLAIM_AMOUNT);

            assertThat(claim.getClaimAmount()).isEqualByComparingTo(Claim.MAX_CLAIM_AMOUNT);
        }

        @Test
        @DisplayName("rejects an amount one cent above the maximum")
        void rejectsAmountJustAboveMax() {
            BigDecimal justOver = Claim.MAX_CLAIM_AMOUNT.add(new BigDecimal("0.01"));

            assertThatThrownBy(() ->
                    new Claim(CLAIM_ID, USER_ID, VALID_DATE, VALID_LOCATION, VALID_DESCRIPTION, justOver))
                    .isInstanceOf(InvalidClaimException.class);
        }

        @Test
        @DisplayName("rejects a null claimId")
        void rejectsNullClaimId() {
            assertThatThrownBy(() ->
                    new Claim(null, USER_ID, VALID_DATE, VALID_LOCATION, VALID_DESCRIPTION, VALID_AMOUNT))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("status transitions")
    class StatusTransitions {

        @Test
        @DisplayName("SUBMITTED -> UNDER_REVIEW is allowed and raises ClaimStatusChanged")
        void allowsSubmittedToUnderReview() {
            Claim claim = newValidClaim();
            claim.clearEvents(); // isolate the status-change event from the creation event

            claim.updateStatus(ClaimStatus.UNDER_REVIEW);

            assertThat(claim.getStatus()).isEqualTo(ClaimStatus.UNDER_REVIEW);
            assertThat(claim.getDomainEvents()).hasSize(1);
            assertThat(claim.getDomainEvents().get(0)).isInstanceOf(ClaimStatusChanged.class);
        }

        @Test
        @DisplayName("SUBMITTED -> APPROVED is rejected (must pass through UNDER_REVIEW)")
        void rejectsSubmittedDirectlyToApproved() {
            Claim claim = newValidClaim();

            assertThatThrownBy(() -> claim.updateStatus(ClaimStatus.APPROVED))
                    .isInstanceOf(InvalidStatusTransitionException.class);
        }

        @Test
        @DisplayName("CLOSED is a terminal state — no further transitions allowed")
        void closedIsTerminal() {
            Claim claim = newValidClaim();
            claim.updateStatus(ClaimStatus.UNDER_REVIEW);
            claim.updateStatus(ClaimStatus.APPROVED);
            claim.updateStatus(ClaimStatus.CLOSED);

            assertThatThrownBy(() -> claim.updateStatus(ClaimStatus.SUBMITTED))
                    .isInstanceOf(InvalidStatusTransitionException.class);
        }

        @Test
        @DisplayName("KNOWN BUG: ClaimStatusChanged.changedBy is populated with the claim owner's " +
                "userId, not the admin performing the update — see UpdateClaimStatusUseCase, " +
                "which never passes the acting admin's id into Claim.updateStatus(). This breaks " +
                "the audit trail: the Kafka event and any consumer of it cannot tell which admin " +
                "approved/rejected a claim.")
        void changedByIsActuallyTheClaimOwnerNotTheAdmin() {
            Claim claim = newValidClaim();
            claim.clearEvents();

            claim.updateStatus(ClaimStatus.UNDER_REVIEW);

            ClaimStatusChanged event = (ClaimStatusChanged) claim.getDomainEvents().get(0);
            // This assertion documents CURRENT (buggy) behaviour: changedBy always equals
            // the claim's own userId, regardless of who actually changed the status.
            assertThat(event.changedBy()).isEqualTo(USER_ID);
        }
    }
}
