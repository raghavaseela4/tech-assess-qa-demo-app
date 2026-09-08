package com.example.demo.domain.claim;

import com.example.demo.domain.DomainEvent;
import com.example.demo.domain.claim.events.ClaimStatusChanged;
import com.example.demo.domain.claim.events.ClaimSubmitted;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Claim {

    public static final BigDecimal MAX_CLAIM_AMOUNT = new BigDecimal("1000000");

    private UUID claimId;
    private UUID userId;
    private LocalDate incidentDate;
    private String incidentLocation;
    private String description;
    private BigDecimal claimAmount;
    private ClaimStatus status;
    private Instant createdAt;
    private Instant updatedAt;
    private final List<DomainEvent> domainEvents = new ArrayList<>();

    private Claim() {
        // Used by reconstitute()
    }

    /**
     * Reconstruct a Claim from persisted state (no validation, no events).
     *
     * <p><b>WARNING: FOR PERSISTENCE LAYER ONLY.</b> This method bypasses all domain validation
     * and does not publish domain events. It should ONLY be used by the infrastructure layer
     * (e.g., ClaimPersistenceMapper) when reconstituting entities from the database.</p>
     *
     * <p>For creating new claims, use the {@link #Claim(UUID, UUID, LocalDate, String, String, BigDecimal)}
     * constructor which enforces business rules and publishes domain events.</p>
     *
     * @param claimId the claim identifier
     * @param userId the user who submitted the claim
     * @param incidentDate the date of the incident
     * @param incidentLocation where the incident occurred
     * @param description details of the incident
     * @param claimAmount the claimed amount
     * @param status the current claim status
     * @param createdAt when the claim was created
     * @param updatedAt when the claim was last updated
     * @return a reconstituted Claim aggregate with no validation
     */
    public static Claim reconstitute(UUID claimId, UUID userId, LocalDate incidentDate,
                                     String incidentLocation, String description,
                                     BigDecimal claimAmount, ClaimStatus status,
                                     Instant createdAt, Instant updatedAt) {
        Claim claim = new Claim();
        claim.claimId = claimId;
        claim.userId = userId;
        claim.incidentDate = incidentDate;
        claim.incidentLocation = incidentLocation;
        claim.description = description;
        claim.claimAmount = claimAmount;
        claim.status = status;
        claim.createdAt = createdAt;
        claim.updatedAt = updatedAt;
        return claim;
    }

    public Claim(UUID claimId, UUID userId, LocalDate incidentDate,
                 String incidentLocation, String description, BigDecimal claimAmount) {
        Objects.requireNonNull(claimId, "claimId must not be null");
        Objects.requireNonNull(userId, "userId must not be null");
        Objects.requireNonNull(incidentDate, "incidentDate must not be null");
        Objects.requireNonNull(description, "description must not be null");
        Objects.requireNonNull(claimAmount, "claimAmount must not be null");
        Objects.requireNonNull(incidentLocation, "incidentLocation must not be null");

        if (incidentDate.isAfter(LocalDate.now())) {
            throw new InvalidClaimException("Incident date cannot be in the future");
        }

        String trimmedDescription = description.trim();
        if (trimmedDescription.length() < 10 || trimmedDescription.length() > 1000) {
            throw new InvalidClaimException("Description must be 10-1000 characters");
        }

        if (claimAmount.compareTo(BigDecimal.ZERO) <= 0
                || claimAmount.compareTo(MAX_CLAIM_AMOUNT) > 0) {
            throw new InvalidClaimException("Claim amount must be positive and <= 1,000,000");
        }

        String trimmedLocation = incidentLocation.trim();
        if (trimmedLocation.length() < 5 || trimmedLocation.length() > 200) {
            throw new InvalidClaimException("Incident location required, 5-200 characters");
        }

        this.claimId = claimId;
        this.userId = userId;
        this.incidentDate = incidentDate;
        this.incidentLocation = trimmedLocation;
        this.description = trimmedDescription;
        this.claimAmount = claimAmount;
        this.status = ClaimStatus.SUBMITTED;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;

        this.domainEvents.add(new ClaimSubmitted(
            UUID.randomUUID().toString(),
            claimId,
            userId,
            incidentDate,
            claimAmount,
            Instant.now()
        ));
    }

    public void updateStatus(ClaimStatus newStatus) {
        if (!this.status.canTransitionTo(newStatus)) {
            throw new InvalidStatusTransitionException(
                String.format("Cannot transition from %s to %s", this.status, newStatus));
        }

        ClaimStatus oldStatus = this.status;
        this.status = newStatus;
        this.updatedAt = Instant.now();

        this.domainEvents.add(new ClaimStatusChanged(
            UUID.randomUUID().toString(),
            this.claimId,
            oldStatus,
            newStatus,
            this.userId,
            Instant.now()
        ));
    }

    public List<DomainEvent> getDomainEvents() {
        return List.copyOf(domainEvents);
    }

    public void clearEvents() {
        domainEvents.clear();
    }

    public UUID getClaimId() { return claimId; }
    public UUID getUserId() { return userId; }
    public LocalDate getIncidentDate() { return incidentDate; }
    public String getIncidentLocation() { return incidentLocation; }
    public String getDescription() { return description; }
    public BigDecimal getClaimAmount() { return claimAmount; }
    public ClaimStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
