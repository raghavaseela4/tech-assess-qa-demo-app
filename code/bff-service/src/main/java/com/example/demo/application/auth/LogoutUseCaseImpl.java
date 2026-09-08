package com.example.demo.application.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class LogoutUseCaseImpl implements LogoutUseCase {

    private static final Logger LOG = LoggerFactory.getLogger(LogoutUseCaseImpl.class);

    private final KeycloakTokenPort keycloakTokenPort;

    public LogoutUseCaseImpl(KeycloakTokenPort keycloakTokenPort) {
        this.keycloakTokenPort = keycloakTokenPort;
    }

    @Override
    @SuppressWarnings("PMD.AvoidCatchingGenericException") // logout must never fail the user — swallow any infra error
    public void logout(String accessToken) {
        try {
            keycloakTokenPort.invalidateSession(accessToken);
        } catch (RuntimeException exception) {
            LOG.warn("Failed to invalidate Keycloak session, continuing with cookie cleanup", exception);
        }
    }
}
