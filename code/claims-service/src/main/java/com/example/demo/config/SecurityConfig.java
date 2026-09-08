package com.example.demo.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Security configuration for Claims Service.
 *
 * <p>Enforces JWT-based authentication for all /api/** endpoints.
 * Extracts roles from Keycloak realm_access.roles claim and maps to Spring Security authorities.
 *
 * <p>RBAC Enforcement Pattern (for future endpoint implementations):
 * <ul>
 *   <li>Use @PreAuthorize("hasRole('ADMIN')") for admin-only endpoints</li>
 *   <li>Use @PreAuthorize("hasRole('CLAIMANT')") for claimant-only endpoints</li>
 *   <li>For user-specific data: Extract userId from JWT subject and validate ownership in business logic</li>
 * </ul>
 *
 * @see org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private static final Logger LOG = LoggerFactory.getLogger(SecurityConfig.class);

    @org.springframework.beans.factory.annotation.Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}")
    private String jwkSetUri;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().permitAll())
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwtAuthenticationConverter(jwtAuthenticationConverter())));

        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        var jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(keycloakGrantedAuthoritiesConverter());
        return jwtAuthenticationConverter;
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
        var timestampValidator = new JwtTimestampValidator(Duration.ofSeconds(30));
        decoder.setJwtValidator(timestampValidator);
        return decoder;
    }

    private Converter<Jwt, Collection<GrantedAuthority>> keycloakGrantedAuthoritiesConverter() {
        return jwt -> {
            LOG.debug("DEBUG JWT: Processing JWT token");
            LOG.debug("DEBUG JWT: Subject: {}", jwt.getSubject());
            LOG.debug("DEBUG JWT: Issuer: {}", jwt.getIssuer());
            LOG.debug("DEBUG JWT: Audience: {}", jwt.getAudience());
            LOG.debug("DEBUG JWT: All claims: {}", jwt.getClaims().keySet());

            Map<String, Object> realmAccess = jwt.getClaim("realm_access");
            if (realmAccess == null) {
                LOG.debug("DEBUG JWT: No realm_access claim found");
                return Collections.emptyList();
            }

            @SuppressWarnings("unchecked")
            List<String> roles = (List<String>) realmAccess.get("roles");
            if (roles == null) {
                LOG.debug("DEBUG JWT: No roles found in realm_access");
                return Collections.emptyList();
            }

            LOG.debug("DEBUG JWT: Found roles: {}", roles);
            return roles.stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase(Locale.ROOT)))
                    .collect(Collectors.toList());
        };
    }
}
