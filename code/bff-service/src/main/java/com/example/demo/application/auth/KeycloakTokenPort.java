package com.example.demo.application.auth;

public interface KeycloakTokenPort {

    TokenResponse getTokens(String email, String password);

    TokenResponse refreshTokens(String refreshToken);

    void invalidateSession(String accessToken);

    record TokenResponse(String accessToken, String refreshToken, int expiresIn, int refreshExpiresIn) {}
}
