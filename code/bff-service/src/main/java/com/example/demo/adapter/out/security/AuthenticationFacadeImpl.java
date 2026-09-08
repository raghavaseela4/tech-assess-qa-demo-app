package com.example.demo.adapter.out.security;

import com.example.demo.application.auth.AuthenticationFacade;
import com.example.demo.application.auth.NoRoleException;
import com.example.demo.application.auth.UnauthenticatedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.UUID;

@Component
public class AuthenticationFacadeImpl implements AuthenticationFacade {

    @Override
    public UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwtAuthenticationToken) {
            Jwt jwt = jwtAuthenticationToken.getToken();
            return UUID.fromString(jwt.getSubject());
        }
        throw new UnauthenticatedException("No authenticated user found");
    }

    @Override
    public String getCurrentUserRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(authority -> authority.startsWith("ROLE_"))
                .findFirst()
                .orElseThrow(() -> new NoRoleException("No role found for authenticated user"));
    }

    @Override
    public boolean hasRole(String role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String expectedAuthority = "ROLE_" + role.toUpperCase(Locale.ROOT);
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals(expectedAuthority));
    }

    @Override
    public boolean isAdmin() {
        return hasRole("ADMIN");
    }

    @Override
    public String getJwtTokenValue() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwtAuthenticationToken) {
            Jwt jwt = jwtAuthenticationToken.getToken();
            return jwt.getTokenValue();
        }
        throw new UnauthenticatedException("No authenticated user found");
    }
}
