package com.example.demo.adapter.out.persistence;

import com.example.demo.domain.claim.Claim;
import com.example.demo.domain.claim.ClaimRepository;
import com.example.demo.domain.claim.ClaimStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * JPA implementation of the ClaimRepository domain interface.
 *
 * <p>This adapter bridges the domain layer (pure Java) with the infrastructure layer (JPA/Hibernate)
 * by mapping between domain {@link Claim} aggregates and JPA {@link ClaimEntity} entities.</p>
 *
 * <p><b>Concurrency Note:</b> This implementation does not use optimistic locking (@Version).
 * For this training application with low concurrency requirements, last-write-wins semantics
 * are acceptable. In production systems with high concurrent updates, consider adding @Version
 * to ClaimEntity for optimistic locking and handling OptimisticLockingFailureException.</p>
 */
@Repository
@RequiredArgsConstructor
public class ClaimRepositoryAdapter implements ClaimRepository {

    private final ClaimJpaRepository jpaRepository;
    private final ClaimPersistenceMapper mapper;

    @Override
    public Claim save(Claim claim) {
        ClaimEntity entity = mapper.toEntity(claim);
        ClaimEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Claim> findById(UUID claimId) {
        return jpaRepository.findById(claimId)
                .map(mapper::toDomain);
    }

    @Override
    public List<Claim> findByUserId(UUID userId) {
        return jpaRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Claim> findAll() {
        return jpaRepository.findAll().stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Claim> findAllByStatus(ClaimStatus status) {
        return jpaRepository.findByStatus(status).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Claim> findAllByStatusAndUserId(ClaimStatus status, UUID userId) {
        return jpaRepository.findByStatusAndUserId(status, userId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsById(UUID claimId) {
        return jpaRepository.existsById(claimId);
    }

    @Override
    public long count() {
        return jpaRepository.count();
    }

    @Override
    public long countByStatus(ClaimStatus status) {
        return jpaRepository.countByStatus(status);
    }

    @Override
    public Map<ClaimStatus, Long> countGroupedByStatus() {
        Map<ClaimStatus, Long> result = new EnumMap<>(ClaimStatus.class);
        for (Object[] row : jpaRepository.countGroupedByStatus()) {
            ClaimStatus status = (ClaimStatus) row[0];
            Long count = (Long) row[1];
            result.put(status, count);
        }
        return result;
    }
}
