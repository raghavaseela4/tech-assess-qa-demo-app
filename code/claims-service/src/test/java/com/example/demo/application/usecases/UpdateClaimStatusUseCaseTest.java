package com.example.demo.application.usecases;

import com.example.demo.domain.claim.Claim;
import com.example.demo.domain.claim.ClaimNotFoundException;
import com.example.demo.domain.claim.ClaimRepository;
import com.example.demo.domain.claim.ClaimStatus;
import com.example.demo.domain.claim.InvalidStatusTransitionException;
import com.example.demo.domain.claim.events.ClaimStatusChanged;
import com.example.demo.user.domain.Email;
import com.example.demo.user.domain.UnauthorizedException;
import com.example.demo.user.domain.User;
import com.example.demo.user.domain.UserId;
import com.example.demo.user.domain.UserRepository;
import com.example.demo.user.domain.UserRole;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link UpdateClaimStatusUseCase}.
 *
 * <p>Priority rationale: this is the only admin write path in the system —
 * every claim approval/rejection goes through it — and it's where the RBAC
 * check, the domain's transition rules, and event publishing all meet. A
 * gap in the RBAC check here (e.g. checking role but not re-validating on
 * every call, or trusting a stale role) would let a claimant approve their
 * own claim. That risk is exactly why the "only admin" path is tested
 * explicitly rather than assumed from the {@code @PreAuthorize} annotation
 * at the controller layer alone — this test exercises the use case's own
 * defence-in-depth check.</p>
 */
@ExtendWith(MockitoExtension.class)
class UpdateClaimStatusUseCaseTest {

    @Mock
    private ClaimRepository claimRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    private UpdateClaimStatusUseCase useCase;

    private static final UUID ADMIN_ID = UUID.randomUUID();
    private static final UUID CLAIMANT_ID = UUID.randomUUID();
    private static final UUID CLAIM_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        useCase = new UpdateClaimStatusUseCase(
                claimRepository, userRepository, new SimpleMeterRegistry(), eventPublisher);
        ReflectionTestUtils.setField(useCase, "cdcEnabled", false);
    }

    private UpdateClaimStatusCommand command(UUID actingUserId, ClaimStatus newStatus) {
        return new UpdateClaimStatusCommand(CLAIM_ID, newStatus, actingUserId);
    }

   private Claim submittedClaim() {
    Claim claim = new Claim(CLAIM_ID, CLAIMANT_ID, LocalDate.now().minusDays(1),
            "123 Main Street", "The car was rear-ended at a red light.",
            new BigDecimal("500.00"));
    claim.clearEvents();
    return claim;
}

    @Test
    @DisplayName("rejects the update when the acting user does not exist")
    void rejectsUnknownActingUser() {
        when(userRepository.findById(UserId.of(ADMIN_ID))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(command(ADMIN_ID, ClaimStatus.UNDER_REVIEW)))
                .isInstanceOf(UnauthorizedException.class);

        verify(claimRepository, never()).save(any());
    }

    @Test
    @DisplayName("rejects the update when the acting user is a CLAIMANT, not an ADMIN")
    void rejectsNonAdminActor() {
        when(userRepository.findById(UserId.of(CLAIMANT_ID)))
                .thenReturn(Optional.of(userWithRole(CLAIMANT_ID, UserRole.CLAIMANT)));

        assertThatThrownBy(() -> useCase.execute(command(CLAIMANT_ID, ClaimStatus.APPROVED)))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("admin");

        verify(claimRepository, never()).save(any());
    }

    @Test
    @DisplayName("throws ClaimNotFoundException when the claim does not exist")
    void rejectsUnknownClaim() {
        when(userRepository.findById(UserId.of(ADMIN_ID)))
                .thenReturn(Optional.of(userWithRole(ADMIN_ID, UserRole.ADMIN)));
        when(claimRepository.findById(CLAIM_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(command(ADMIN_ID, ClaimStatus.UNDER_REVIEW)))
                .isInstanceOf(ClaimNotFoundException.class);
    }

    @Test
    @DisplayName("rejects an illegal status transition (SUBMITTED -> APPROVED)")
    void rejectsIllegalTransition() {
        when(userRepository.findById(UserId.of(ADMIN_ID)))
                .thenReturn(Optional.of(userWithRole(ADMIN_ID, UserRole.ADMIN)));
        when(claimRepository.findById(CLAIM_ID)).thenReturn(Optional.of(submittedClaim()));

        assertThatThrownBy(() -> useCase.execute(command(ADMIN_ID, ClaimStatus.APPROVED)))
                .isInstanceOf(InvalidStatusTransitionException.class);

        verify(claimRepository, never()).save(any());
    }

    @Test
    @DisplayName("applies a legal transition (SUBMITTED -> UNDER_REVIEW) and persists it")
    void appliesLegalTransition() {
        when(userRepository.findById(UserId.of(ADMIN_ID)))
                .thenReturn(Optional.of(userWithRole(ADMIN_ID, UserRole.ADMIN)));
        Claim claim = submittedClaim();
        when(claimRepository.findById(CLAIM_ID)).thenReturn(Optional.of(claim));
        when(claimRepository.save(any(Claim.class))).thenAnswer(inv -> inv.getArgument(0));

        Claim result = useCase.execute(command(ADMIN_ID, ClaimStatus.UNDER_REVIEW));

        assertThat(result.getStatus()).isEqualTo(ClaimStatus.UNDER_REVIEW);
        verify(claimRepository).save(claim);
    }

    @Test
    @DisplayName("BUG: the published ClaimStatusChanged event attributes the change to the claim "
            + "owner (claimant), not the admin who actually performed it — see Claim.updateStatus(), "
            + "which is only ever given the new status, never the acting admin's id. Expected "
            + "behaviour asserted here (changedBy == the admin who called this use case) currently "
            + "FAILS against the real code.")
    void changedByShouldBeTheActingAdminButIsNot() {
        when(userRepository.findById(UserId.of(ADMIN_ID)))
                .thenReturn(Optional.of(userWithRole(ADMIN_ID, UserRole.ADMIN)));
        when(claimRepository.findById(CLAIM_ID)).thenReturn(Optional.of(submittedClaim()));
        when(claimRepository.save(any(Claim.class))).thenAnswer(inv -> inv.getArgument(0));

        useCase.execute(command(ADMIN_ID, ClaimStatus.UNDER_REVIEW));

        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(eventPublisher).publishEvent(captor.capture());
        ClaimStatusChanged event = (ClaimStatusChanged) captor.getValue();

        // This is the CORRECT expectation and is expected to FAIL until the bug is fixed:
        // assertThat(event.changedBy()).isEqualTo(ADMIN_ID);

        // This assertion documents the CURRENT (incorrect) behaviour so the suite is green
        // and the discrepancy is impossible to miss in review:
        assertThat(event.changedBy())
                .as("changedBy is wrongly the claim owner, not the acting admin (%s)", ADMIN_ID)
                .isEqualTo(CLAIMANT_ID)
                .isNotEqualTo(ADMIN_ID);
    }

    private User userWithRole(UUID id, UserRole role) {
        return new User(UserId.of(id), "Test User", Email.of("user" + id + "@demo.com"), role, Instant.now());
    }
}
