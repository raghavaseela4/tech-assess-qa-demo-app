package com.example.demo.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Security configuration for the contract-test validation stack.
 *
 * <p>Permits all requests without Keycloak authentication and injects a static test
 * {@link JwtAuthenticationToken} so downstream code that reads the JWT can function
 * without a real Keycloak instance.
 *
 * <p>SECURITY WARNING: This profile must NEVER be used in production or any deployed environment.
 */
@Configuration
@Profile("contract-test")
public class ContractTestSecurityConfiguration {

    private static final Logger LOG =
            LoggerFactory.getLogger(ContractTestSecurityConfiguration.class);

    private static final String TEST_USER_SUB = "550e8400-e29b-41d4-a716-446655440000";
    private static final String TEST_TOKEN_VALUE = "test-token-for-validation";

    @Bean
    @Order(1)
    public SecurityFilterChain contractTestFilterChain(HttpSecurity http) throws Exception {
        LOG.warn("CONTRACT-TEST PROFILE ACTIVE — all endpoints permit unauthenticated access");
        http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .oauth2ResourceServer(AbstractHttpConfigurer::disable)
                .addFilterBefore(
                        contractTestJwtInjectionFilter(), AnonymousAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public OncePerRequestFilter contractTestJwtInjectionFilter() {
        return new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(
                    HttpServletRequest request,
                    HttpServletResponse response,
                    FilterChain filterChain)
                    throws ServletException, IOException {
                if (SecurityContextHolder.getContext().getAuthentication() == null) {
                    Instant now = Instant.now();
                    Jwt jwt =
                            Jwt.withTokenValue(TEST_TOKEN_VALUE)
                                    .header("alg", "none")
                                    .claim("sub", TEST_USER_SUB)
                                    .claim("iss", "contract-test")
                                    .claim(
                                            "realm_access",
                                            java.util.Map.of(
                                                    "roles",
                                                    List.of("ADMIN", "CLAIMANT")))
                                    .issuedAt(now)
                                    .expiresAt(now.plusSeconds(3600))
                                    .claim("scope", List.of("openid"))
                                    .claim("preferred_username", "contract-test-user")
                                    .build();
                    var authorities =
                            List.of(
                                    new SimpleGrantedAuthority("ROLE_ADMIN"),
                                    new SimpleGrantedAuthority("ROLE_CLAIMANT"));
                    var authentication =
                            new JwtAuthenticationToken(jwt, authorities, TEST_USER_SUB);
                    var context = SecurityContextHolder.createEmptyContext();
                    context.setAuthentication(authentication);
                    SecurityContextHolder.setContext(context);
                }
                filterChain.doFilter(request, response);
            }
        };
    }
}
