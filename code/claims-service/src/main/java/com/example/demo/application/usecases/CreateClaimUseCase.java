package com.example.demo.application.usecases;

import com.example.demo.application.exceptions.UserNotFoundException;
import com.example.demo.domain.claim.Claim;
import com.example.demo.domain.claim.ClaimRepository;
import com.example.demo.user.domain.UserId;
import com.example.demo.user.domain.UserRepository;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class CreateClaimUseCase {

    private static final Logger LOG = LoggerFactory.getLogger(CreateClaimUseCase.class);

    private final ClaimRepository claimRepository;
    private final UserRepository userRepository;
    private final MeterRegistry meterRegistry;
    private final ApplicationEventPublisher eventPublisher;

    @Value("${app.events.cdc-enabled:true}")
    private boolean cdcEnabled;

    public Claim execute(CreateClaimCommand command) {
        userRepository.findById(UserId.of(command.userId()))
                .orElseThrow(() -> new UserNotFoundException(command.userId()));

        Claim claim = new Claim(
                UUID.randomUUID(),
                command.userId(),
                command.incidentDate(),
                command.incidentLocation(),
                command.description(),
                command.claimAmount()
        );

        Claim savedClaim = claimRepository.save(claim);

        if (cdcEnabled) {
            LOG.info("CDC enabled — event will be published by Debezium for claimId={}", savedClaim.getClaimId());
        } else {
            LOG.info("CDC disabled — publishing {} event(s) directly for claimId={}",
                    claim.getDomainEvents().size(), savedClaim.getClaimId());
            claim.getDomainEvents().forEach(eventPublisher::publishEvent);
            claim.clearEvents();
        }

        meterRegistry.counter("claims.submitted",
                "status", savedClaim.getStatus().name(),
                "userId", savedClaim.getUserId().toString()
        ).increment();

        return savedClaim;
    }
}
