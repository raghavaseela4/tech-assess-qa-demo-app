package com.example.demo.application.auth;

import org.springframework.stereotype.Service;

@Service
public class RefreshTokenUseCaseImpl implements RefreshTokenUseCase {

    private final KeycloakTokenPort keycloakTokenPort;

    public RefreshTokenUseCaseImpl(KeycloakTokenPort keycloakTokenPort) {
        this.keycloakTokenPort = keycloakTokenPort;
    }

    @Override
    public RefreshTokenResult refresh(String refreshToken) {
        var tokens = keycloakTokenPort.refreshTokens(refreshToken);
        return new RefreshTokenResult(tokens.accessToken(), tokens.refreshToken());
    }
}
