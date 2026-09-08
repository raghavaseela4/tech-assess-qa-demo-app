package com.example.demo.application.auth;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

public record SignupResult(UUID userId, String email, String name, String role, OffsetDateTime createdAt) {
    public SignupResult {
        Objects.requireNonNull(userId, "UserId must not be null");
        Objects.requireNonNull(email, "Email must not be null");
        Objects.requireNonNull(name, "Name must not be null");
        Objects.requireNonNull(role, "Role must not be null");
        Objects.requireNonNull(createdAt, "CreatedAt must not be null");
    }
}
