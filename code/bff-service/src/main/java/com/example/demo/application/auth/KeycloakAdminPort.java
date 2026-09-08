package com.example.demo.application.auth;

import java.util.UUID;

/**
 * Port interface for Keycloak administrative operations.
 * Adapter implementations handle communication with Keycloak Admin API.
 */
public interface KeycloakAdminPort {

    /**
     * Creates a new user in Keycloak with the specified credentials and role.
     *
     * @param email user's email address (used as username)
     * @param password user's password
     * @param name user's full name
     * @param role user's role in the system (e.g., "CLAIMANT", "ADMIN")
     * @return UUID of the created user in Keycloak
     * @throws UserAlreadyExistsException if email already exists in Keycloak
     * @throws ExternalServiceException if Keycloak API call fails
     */
    UUID createUser(String email, String password, String name, String role);
}
