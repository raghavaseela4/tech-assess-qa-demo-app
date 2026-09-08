package com.example.demo.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ClaimJpaRepository extends JpaRepository<ClaimEntity, UUID> {

    @Query("SELECT c FROM ClaimEntity c WHERE c.userId = :userId ORDER BY c.createdAt DESC")
    List<ClaimEntity> findByUserIdOrderByCreatedAtDesc(@Param("userId") UUID userId);

    @Query("SELECT c FROM ClaimEntity c WHERE c.status = :status ORDER BY c.createdAt DESC")
    List<ClaimEntity> findByStatus(@Param("status") com.example.demo.domain.claim.ClaimStatus status);

    @Query("SELECT c FROM ClaimEntity c WHERE c.status = :status AND c.userId = :userId ORDER BY c.createdAt DESC")
    List<ClaimEntity> findByStatusAndUserId(
            @Param("status") com.example.demo.domain.claim.ClaimStatus status,
            @Param("userId") UUID userId);

    long countByStatus(@Param("status") com.example.demo.domain.claim.ClaimStatus status);

    @Query("SELECT c.status, COUNT(c) FROM ClaimEntity c GROUP BY c.status")
    List<Object[]> countGroupedByStatus();
}
