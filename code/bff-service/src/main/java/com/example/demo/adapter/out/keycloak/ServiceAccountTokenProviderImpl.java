package com.example.demo.adapter.out.keycloak;

import com.example.demo.application.auth.ServiceAccountTokenProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;

@Component
public class ServiceAccountTokenProviderImpl implements ServiceAccountTokenProvider {

    private static final long TOKEN_REFRESH_BUFFER_SECONDS = 60;

    private final RestClient restClient;
    private final String tokenUrl;
    private final String clientId;
    private final String clientSecret;

    private String cachedToken;
    private Instant tokenExpiry;

    public ServiceAccountTokenProviderImpl(
            RestClient.Builder restClientBuilder,
            @Value("${keycloak.service-account.token-url}") String tokenUrl,
            @Value("${keycloak.service-account.client-id}") String clientId,
            @Value("${keycloak.service-account.client-secret}") String clientSecret
    ) {
        this.restClient = restClientBuilder.build();
        this.tokenUrl = tokenUrl;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    @Override
    public String getServiceAccountToken() {
        synchronized (this) {
            if (cachedToken != null && tokenExpiry != null
                    && Instant.now().plusSeconds(TOKEN_REFRESH_BUFFER_SECONDS).isBefore(tokenExpiry)) {
                return cachedToken;
            }
            return refreshToken();
        }
    }

    @Override
    public String forceRefreshToken() {
        synchronized (this) {
            cachedToken = null;
            tokenExpiry = null;
            return refreshToken();
        }
    }

    private String refreshToken() {
        var formBody = "grant_type=client_credentials"
                + "&client_id=" + URLEncoder.encode(clientId, StandardCharsets.UTF_8)
                + "&client_secret=" + URLEncoder.encode(clientSecret, StandardCharsets.UTF_8);

        @SuppressWarnings("unchecked")
        Map<String, Object> response = restClient.post()
                .uri(tokenUrl)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(formBody)
                .retrieve()
                .body(Map.class);

        if (response == null || !response.containsKey("access_token")) {
            throw new IllegalStateException("Failed to obtain service account token from Keycloak");
        }

        cachedToken = (String) response.get("access_token");
        Integer expiresIn = (Integer) response.get("expires_in");
        if (expiresIn == null) {
            throw new IllegalStateException("Keycloak token response missing 'expires_in' field");
        }
        tokenExpiry = Instant.now().plusSeconds(expiresIn);

        return cachedToken;
    }
}
