package com.example.demo.adapter.out.keycloak;

import com.example.demo.application.auth.InvalidCredentialsException;
import com.example.demo.application.auth.KeycloakTokenPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Component
public class KeycloakTokenAdapter implements KeycloakTokenPort {

    private final RestClient restClient;
    private final String tokenUrl;
    private final String logoutUrl;
    private final String clientId;

    public KeycloakTokenAdapter(
            RestClient.Builder restClientBuilder,
            @Value("${keycloak.token.url}") String tokenUrl,
            @Value("${keycloak.logout.url}") String logoutUrl,
            @Value("${keycloak.token.client-id:demo-app-ui}") String clientId
    ) {
        this.restClient = restClientBuilder.build();
        this.tokenUrl = tokenUrl;
        this.logoutUrl = logoutUrl;
        this.clientId = clientId;
    }

    @Override
    public TokenResponse getTokens(String email, String password) {
        var formBody = "grant_type=password"
                + "&username=" + URLEncoder.encode(email, StandardCharsets.UTF_8)
                + "&password=" + URLEncoder.encode(password, StandardCharsets.UTF_8)
                + "&client_id=" + URLEncoder.encode(clientId, StandardCharsets.UTF_8);
        return postToTokenEndpoint(formBody);
    }

    @Override
    public TokenResponse refreshTokens(String refreshToken) {
        var formBody = "grant_type=refresh_token"
                + "&refresh_token=" + URLEncoder.encode(refreshToken, StandardCharsets.UTF_8)
                + "&client_id=" + URLEncoder.encode(clientId, StandardCharsets.UTF_8);
        return postToTokenEndpoint(formBody);
    }

    @SuppressWarnings("unchecked")
    private TokenResponse postToTokenEndpoint(String formBody) {
        try {
            Map<String, Object> response = restClient.post()
                    .uri(tokenUrl)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(formBody)
                    .retrieve()
                    .body(Map.class);

            if (response == null || !response.containsKey("access_token")) {
                throw new InvalidCredentialsException();
            }

            return new TokenResponse(
                    (String) response.get("access_token"),
                    (String) response.get("refresh_token"),
                    (Integer) response.get("expires_in"),
                    (Integer) response.get("refresh_expires_in")
            );
        } catch (HttpClientErrorException exception) {
            throw new InvalidCredentialsException(exception);
        }
    }

    @Override
    public void invalidateSession(String accessToken) {
        var formBody = "client_id=" + clientId
                + "&token=" + accessToken;

        restClient.post()
                .uri(logoutUrl)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .body(formBody)
                .retrieve()
                .toBodilessEntity();
    }
}
