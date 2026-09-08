package com.example.demo.application.auth;

import java.util.Objects;

public record RefreshTokenResult(
        String accessToken,
        String refreshToken
) {
    public RefreshTokenResult {
        Objects.requireNonNull(accessToken, "AccessToken must not be null");
        Objects.requireNonNull(refreshToken, "RefreshToken must not be null");
    }
}
