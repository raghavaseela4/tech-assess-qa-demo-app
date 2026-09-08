package com.example.demo.adapter.out.keycloak;

import com.example.demo.application.auth.ExternalServiceException;
import com.example.demo.application.auth.KeycloakAdminPort;
import com.example.demo.application.auth.UserAlreadyExistsException;
import jakarta.ws.rs.core.Response;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Component
public class KeycloakAdminAdapter implements KeycloakAdminPort {

    private final Keycloak keycloak;
    private final String realm;

    public KeycloakAdminAdapter(
            Keycloak keycloak,
            @Value("${keycloak.admin.target-realm:demo-app}") String realm
    ) {
        this.keycloak = keycloak;
        this.realm = realm;
    }

    @Override
    public UUID createUser(String email, String password, String name, String role) {
        RealmResource realmResource = keycloak.realm(realm);

        var credential = new CredentialRepresentation();
        credential.setTemporary(false);
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(password);

        var userRepresentation = new UserRepresentation();
        userRepresentation.setEmail(email);
        userRepresentation.setUsername(email);
        userRepresentation.setFirstName(extractFirstName(name));
        userRepresentation.setLastName(extractLastName(name));
        userRepresentation.setEnabled(true);
        userRepresentation.setEmailVerified(true);
        userRepresentation.setCredentials(List.of(credential));
        userRepresentation.setRequiredActions(List.of()); // Clear all required actions so user can login immediately

        try (Response response = realmResource.users().create(userRepresentation)) {
            if (response.getStatus() == 409) {
                throw new UserAlreadyExistsException(email);
            }
            if (response.getStatus() != 201) {
                throw new ExternalServiceException("Keycloak", "User creation failed: HTTP " + response.getStatus());
            }

            var locationHeader = response.getHeaderString("Location");
            var userId = extractUserIdFromLocation(locationHeader);

            assignRole(realmResource, userId, role.toLowerCase(Locale.ROOT));

            return UUID.fromString(userId);
        }
    }

    private void assignRole(RealmResource realmResource, String userId, String roleName) {
        RoleRepresentation roleRepresentation = realmResource.roles().get(roleName).toRepresentation();
        realmResource.users().get(userId).roles().realmLevel().add(List.of(roleRepresentation));
    }

    private String extractUserIdFromLocation(String locationHeader) {
        if (locationHeader == null) {
            throw new ExternalServiceException("Keycloak", "User location header missing in response");
        }
        return locationHeader.substring(locationHeader.lastIndexOf('/') + 1);
    }

    private String extractFirstName(String fullName) {
        var parts = fullName.trim().split("\\s+", 2);
        return parts[0];
    }

    private String extractLastName(String fullName) {
        var parts = fullName.trim().split("\\s+", 2);
        return parts.length > 1 ? parts[1] : "User"; // Default to "User" if no last name provided
    }
}
