package com.example.demo.adapter.out.persistence;

import com.example.demo.domain.claim.ClaimStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "claims")
@Getter
@Setter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ClaimEntity {

    @Id
    @Column(name = "claim_id", nullable = false)
    @EqualsAndHashCode.Include
    private UUID claimId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "incident_date", nullable = false)
    private LocalDate incidentDate;

    @Column(name = "incident_location", nullable = false, length = 200)
    private String incidentLocation;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "claim_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal claimAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private ClaimStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
