package com.example.demo.application.usecases;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public record CreateClaimCommand(
        UUID userId,
        LocalDate incidentDate,
        String incidentLocation,
        String description,
        BigDecimal claimAmount
) {

    public CreateClaimCommand {
        Objects.requireNonNull(userId, "userId must not be null");
        Objects.requireNonNull(incidentDate, "incidentDate must not be null");
        Objects.requireNonNull(incidentLocation, "incidentLocation must not be null");
        Objects.requireNonNull(description, "description must not be null");
        Objects.requireNonNull(claimAmount, "claimAmount must not be null");
    }
}
