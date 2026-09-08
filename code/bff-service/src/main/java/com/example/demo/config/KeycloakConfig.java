package com.example.demo.config;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KeycloakConfig {

    @Bean
    public Keycloak keycloak(
            @Value("${keycloak.admin.server-url}") String serverUrl,
            @Value("${keycloak.admin.realm:master}") String adminRealm,
            @Value("${keycloak.admin.username}") String username,
            @Value("${keycloak.admin.password}") String password,
            @Value("${keycloak.admin.client-id:admin-cli}") String clientId
    ) {
        return KeycloakBuilder.builder()
                .serverUrl(serverUrl)
                .realm(adminRealm)
                .username(username)
                .password(password)
                .clientId(clientId)
                .build();
    }
}
