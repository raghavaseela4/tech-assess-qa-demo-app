package com.example.demo.application.auth;

import org.springframework.stereotype.Service;

@Service
public class SignupUseCaseImpl implements SignupUseCase {

    private static final String DEFAULT_ROLE = "CLAIMANT";

    private final KeycloakAdminPort keycloakAdminPort;
    private final ClaimsServicePort claimsServicePort;

    public SignupUseCaseImpl(KeycloakAdminPort keycloakAdminPort, ClaimsServicePort claimsServicePort) {
        this.keycloakAdminPort = keycloakAdminPort;
        this.claimsServicePort = claimsServicePort;
    }

    @Override
    public SignupResult signup(SignupCommand command) {
        var keycloakUserId = keycloakAdminPort.createUser(
                command.email(),
                command.password(),
                command.name(),
                DEFAULT_ROLE
        );

        var userRecord = claimsServicePort.createUser(
                keycloakUserId,
                command.email(),
                command.name(),
                DEFAULT_ROLE
        );

        return new SignupResult(
                userRecord.userId(),
                userRecord.email(),
                userRecord.name(),
                userRecord.role(),
                userRecord.createdAt()
        );
    }
}
