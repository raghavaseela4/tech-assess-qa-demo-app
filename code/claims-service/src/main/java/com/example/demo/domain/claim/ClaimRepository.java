package com.example.demo.domain.claim;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface ClaimRepository {

    Claim save(Claim claim);

    Optional<Claim> findById(UUID claimId);

    List<Claim> findByUserId(UUID userId);

    List<Claim> findAll();

    List<Claim> findAllByStatus(ClaimStatus status);

    List<Claim> findAllByStatusAndUserId(ClaimStatus status, UUID userId);

    boolean existsById(UUID claimId);

    long count();

    long countByStatus(ClaimStatus status);

    Map<ClaimStatus, Long> countGroupedByStatus();
}
