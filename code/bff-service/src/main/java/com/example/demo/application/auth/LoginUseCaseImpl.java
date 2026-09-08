package com.example.demo.application.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class LoginUseCaseImpl implements LoginUseCase {

    private final KeycloakTokenPort keycloakTokenPort;
    private final ClaimsServicePort claimsServicePort;
    private final ObjectMapper objectMapper;

    public LoginUseCaseImpl(
            KeycloakTokenPort keycloakTokenPort,
            ClaimsServicePort claimsServicePort,
            ObjectMapper objectMapper
    ) {
        this.keycloakTokenPort = keycloakTokenPort;
        this.claimsServicePort = claimsServicePort;
        this.objectMapper = objectMapper;
    }

    @Override
    public LoginResult login(LoginCommand command) {
        var tokens = keycloakTokenPort.getTokens(command.email(), command.password());

        var userId = extractUserIdFromToken(tokens.accessToken());
        var userDetails = extractUserDetailsFromToken(tokens.accessToken());

        var userRecord = getUserOrAutoProvision(userId, userDetails);

        return new LoginResult(
                tokens.accessToken(),
                tokens.refreshToken(),
                userRecord.userId(),
                userRecord.email(),
                userRecord.name(),
                userRecord.role(),
                userRecord.createdAt()
        );
    }

    private ClaimsServicePort.UserRecord getUserOrAutoProvision(UUID userId, UserDetails userDetails) {
        try {
            return claimsServicePort.getUser(userId);
        } catch (IllegalStateException e) {
            return claimsServicePort.createUser(
                    userId,
                    userDetails.email(),
                    userDetails.name(),
                    userDetails.role()
            );
        }
    }

    @SuppressWarnings("unchecked")
    private UserDetails extractUserDetailsFromToken(String accessToken) {
        var parts = accessToken.split("\\.");
        if (parts.length < 2) {
            throw new InvalidCredentialsException();
        }
        try {
            var payload = new String(Base64.getUrlDecoder().decode(parts[1]));
            var claims = objectMapper.readValue(payload, Map.class);

            var email = (String) claims.get("email");
            var preferredUsername = (String) claims.get("preferred_username");
            var name = (String) claims.getOrDefault("name", preferredUsername);

            var role = extractRoleFromToken(claims);

            return new UserDetails(email, name, role);
        } catch (java.io.IOException exception) {
            throw new InvalidCredentialsException(exception);
        }
    }

    @SuppressWarnings("unchecked")
    private String extractRoleFromToken(Map<String, Object> claims) {
        var realmAccess = (Map<String, Object>) claims.get("realm_access");
        if (realmAccess == null) {
            return "CLAIMANT";
        }
        List<String> roles = (List<String>) realmAccess.get("roles");
        if (roles == null) {
            return "CLAIMANT";
        }
        if (roles.contains("admin")) {
            return "ADMIN";
        }
        if (roles.contains("claimant")) {
            return "CLAIMANT";
        }
        return "CLAIMANT";
    }

    private record UserDetails(String email, String name, String role) {}

    @SuppressWarnings("unchecked")
    private UUID extractUserIdFromToken(String accessToken) {
        var parts = accessToken.split("\\.");
        if (parts.length < 2) {
            throw new InvalidCredentialsException();
        }
        try {
            var payload = new String(Base64.getUrlDecoder().decode(parts[1]));
            var claims = objectMapper.readValue(payload, Map.class);
            var subject = (String) claims.get("sub");
            return UUID.fromString(subject);
        } catch (java.io.IOException exception) {
            throw new InvalidCredentialsException(exception);
        }
    }
}
