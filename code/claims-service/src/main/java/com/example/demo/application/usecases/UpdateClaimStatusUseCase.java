package com.example.demo.application.usecases;

import com.example.demo.domain.claim.Claim;
import com.example.demo.domain.claim.ClaimNotFoundException;
import com.example.demo.domain.claim.ClaimRepository;
import com.example.demo.domain.claim.ClaimStatus;
import com.example.demo.user.domain.UnauthorizedException;
import com.example.demo.user.domain.UserId;
import com.example.demo.user.domain.UserRole;
import com.example.demo.user.domain.UserRepository;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class UpdateClaimStatusUseCase {

    private static final Logger LOG = LoggerFactory.getLogger(UpdateClaimStatusUseCase.class);

    private final ClaimRepository claimRepository;
    private final UserRepository userRepository;
    private final MeterRegistry meterRegistry;
    private final ApplicationEventPublisher eventPublisher;

    @Value("${app.events.cdc-enabled:true}")
    private boolean cdcEnabled;

    public Claim execute(UpdateClaimStatusCommand command) {
        var admin = userRepository.findById(UserId.of(command.adminUserId()))
            .orElseThrow(() -> new UnauthorizedException("User not found or not authorized"));

        if (admin.getRole() != UserRole.ADMIN) {
            throw new UnauthorizedException("Only admin users can update claim status");
        }

        Claim claim = claimRepository.findById(command.claimId())
            .orElseThrow(() -> new ClaimNotFoundException(command.claimId()));

        ClaimStatus oldStatus = claim.getStatus();
        claim.updateStatus(command.newStatus());

        Claim updatedClaim = claimRepository.save(claim);

        if (cdcEnabled) {
            LOG.info("CDC enabled — status change will be published by Debezium for claimId={}, {} -> {}",
                    updatedClaim.getClaimId(), oldStatus, command.newStatus());
        } else {
            LOG.info("CDC disabled — publishing {} event(s) directly for claimId={}, {} -> {}",
                    claim.getDomainEvents().size(), updatedClaim.getClaimId(), oldStatus, command.newStatus());
            claim.getDomainEvents().forEach(eventPublisher::publishEvent);
            claim.clearEvents();
        }

        meterRegistry.counter("claims.status.changed",
                "oldStatus", oldStatus.name(),
                "newStatus", command.newStatus().name(),
                "adminUserId", command.adminUserId().toString()
        ).increment();

        return updatedClaim;
    }
}
