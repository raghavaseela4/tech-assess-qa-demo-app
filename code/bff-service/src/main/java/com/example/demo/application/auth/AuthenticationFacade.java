package com.example.demo.application.auth;

import java.util.UUID;

public interface AuthenticationFacade {

    UUID getCurrentUserId();

    String getCurrentUserRole();

    boolean hasRole(String role);

    boolean isAdmin();

    String getJwtTokenValue();
}
