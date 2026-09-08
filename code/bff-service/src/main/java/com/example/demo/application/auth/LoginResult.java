package com.example.demo.application.auth;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

public record LoginResult(
        String accessToken,
        String refreshToken,
        UUID userId,
        String email,
        String name,
        String role,
        OffsetDateTime createdAt
) {
    public LoginResult {
        Objects.requireNonNull(accessToken, "AccessToken must not be null");
        Objects.requireNonNull(refreshToken, "RefreshToken must not be null");
        Objects.requireNonNull(userId, "UserId must not be null");
        Objects.requireNonNull(email, "Email must not be null");
        Objects.requireNonNull(name, "Name must not be null");
        Objects.requireNonNull(role, "Role must not be null");
        Objects.requireNonNull(createdAt, "CreatedAt must not be null");
    }
}
