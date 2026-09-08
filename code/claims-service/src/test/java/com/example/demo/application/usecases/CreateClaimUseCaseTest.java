package com.example.demo.application.usecases;

import com.example.demo.application.exceptions.UserNotFoundException;
import com.example.demo.domain.claim.Claim;
import com.example.demo.domain.claim.ClaimRepository;
import com.example.demo.user.domain.User;
import com.example.demo.user.domain.UserId;
import com.example.demo.user.domain.UserRepository;
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
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link CreateClaimUseCase}.
 *
 * <p>Priority rationale: this is the write path every claim submission goes
 * through, and it is the integration point between three things that can
 * each independently break the flow: user existence (BFF/Keycloak
 * provisioning), the domain's own validation, and the CDC-vs-direct-publish
 * event toggle (already confirmed misconfigured once — see test execution
 * report Part 6). The event-publishing toggle in particular is exactly the
 * kind of "brittle integration point" this assessment is meant to target:
 * it's a boolean flag that silently changes which of two very different
 * code paths runs, with no compile-time signal if it's ever wrong.</p>
 */
@ExtendWith(MockitoExtension.class)
class CreateClaimUseCaseTest {

    @Mock
    private ClaimRepository claimRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    private CreateClaimUseCase useCase;

    private static final UUID USER_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        useCase = new CreateClaimUseCase(
                claimRepository, userRepository, new SimpleMeterRegistry(), eventPublisher);
    }

    private CreateClaimCommand validCommand() {
        return new CreateClaimCommand(
                USER_ID,
                LocalDate.now().minusDays(1),
                "123 Main Street",
                "The car was rear-ended at a red light.",
                new BigDecimal("500.00"));
    }

    @Test
    @DisplayName("throws UserNotFoundException when the submitting user does not exist")
    void rejectsUnknownUser() {
        when(userRepository.findById(UserId.of(USER_ID))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(validCommand()))
                .isInstanceOf(UserNotFoundException.class);

        verify(claimRepository, never()).save(any());
    }

    @Test
    @DisplayName("CDC disabled: publishes the ClaimSubmitted domain event directly")
    void publishesEventDirectlyWhenCdcDisabled() {
        ReflectionTestUtils.setField(useCase, "cdcEnabled", false);
        when(userRepository.findById(UserId.of(USER_ID)))
                .thenReturn(Optional.of(mockUser()));
        when(claimRepository.save(any(Claim.class))).thenAnswer(inv -> inv.getArgument(0));

        useCase.execute(validCommand());

        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(eventPublisher, times(1)).publishEvent(captor.capture());
        assertThat(captor.getValue()).isInstanceOf(com.example.demo.domain.claim.events.ClaimSubmitted.class);
    }

    @Test
    @DisplayName("CDC enabled: does NOT publish directly — relies on Debezium instead")
    void skipsDirectPublishWhenCdcEnabled() {
        ReflectionTestUtils.setField(useCase, "cdcEnabled", true);
        when(userRepository.findById(UserId.of(USER_ID)))
                .thenReturn(Optional.of(mockUser()));
        when(claimRepository.save(any(Claim.class))).thenAnswer(inv -> inv.getArgument(0));

        useCase.execute(validCommand());

        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("persists a claim with SUBMITTED status via the repository")
    void savesClaimInSubmittedStatus() {
        ReflectionTestUtils.setField(useCase, "cdcEnabled", true);
        when(userRepository.findById(UserId.of(USER_ID))).thenReturn(Optional.of(mockUser()));
        when(claimRepository.save(any(Claim.class))).thenAnswer(inv -> inv.getArgument(0));

        Claim result = useCase.execute(validCommand());

        assertThat(result.getStatus()).isEqualTo(com.example.demo.domain.claim.ClaimStatus.SUBMITTED);
        assertThat(result.getUserId()).isEqualTo(USER_ID);
        verify(claimRepository, times(1)).save(any(Claim.class));
    }

    private User mockUser() {
        return new User(
                UserId.of(USER_ID),
                "Test Claimant",
                com.example.demo.user.domain.Email.of("claimant@demo.com"),
                com.example.demo.user.domain.UserRole.CLAIMANT,
                java.time.Instant.now());
    }
}
