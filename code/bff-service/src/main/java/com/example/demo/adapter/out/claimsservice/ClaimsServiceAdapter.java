package com.example.demo.adapter.out.claimsservice;

import com.example.demo.application.auth.ClaimsServicePort;
import com.example.demo.application.auth.ServiceAccountTokenProvider;
import com.example.demo.application.auth.UserAlreadyExistsException;
import com.example.bff.adapter.out.claims.ApiClient;
import com.example.bff.adapter.out.claims.ApiException;
import com.example.bff.adapter.out.claims.generated.ClaimsApi;
import com.example.bff.adapter.out.claims.generated.UsersApi;
import com.example.bff.adapter.out.claims.generated.model.ClaimResponseDTO;
import com.example.bff.adapter.out.claims.generated.model.ClaimSummaryResponseDTO;
import com.example.bff.adapter.out.claims.generated.model.CreateClaimRequestDTO;
import com.example.bff.adapter.out.claims.generated.model.CreateUserRequestDTO;
import com.example.bff.adapter.out.claims.generated.model.UserResponseDTO;
import com.example.bff.adapter.out.claims.generated.model.UserRoleDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Component
public class ClaimsServiceAdapter implements ClaimsServicePort {

    private static final Logger LOG = LoggerFactory.getLogger(ClaimsServiceAdapter.class);

    private final UsersApi usersApi;
    private final ClaimsApi claimsApi;
    private final ApiClient apiClient;
    private final ServiceAccountTokenProvider serviceAccountTokenProvider;
    private final ObjectMapper objectMapper;

    public ClaimsServiceAdapter(
            ApiClient apiClient,
            ServiceAccountTokenProvider serviceAccountTokenProvider,
            ObjectMapper objectMapper
    ) {
        this.apiClient = apiClient;
        LOG.debug("ClaimsServiceAdapter initialized with URL: {}", apiClient.getBasePath());
        this.usersApi = new UsersApi(apiClient);
        this.claimsApi = new ClaimsApi(apiClient);
        this.serviceAccountTokenProvider = serviceAccountTokenProvider;
        this.objectMapper = objectMapper;
    }

    @Override
    public UserRecord createUser(UUID userId, String email, String name, String role) {
        setServiceAccountBearerToken();

        var request = new CreateUserRequestDTO();
        request.setUserId(userId);
        request.setEmail(email);
        request.setName(name);
        request.setRole(UserRoleDTO.fromValue(role));

        LOG.debug("About to call Claims Service POST /api/users: email={}, name={}, role={}", email, name, role);

        try {
            var response = usersApi.createUser(request);
            LOG.debug("Claims Service call succeeded, userId={}", response.getUserId());
            return mapToUserRecord(response);
        } catch (ApiException exception) {
            LOG.debug("Claims Service call failed with status {}, body={}", exception.getCode(), exception.getResponseBody());
            return handleCreateUserException(exception, email, request);
        }
    }

    private UserRecord handleCreateUserException(ApiException exception, String email, CreateUserRequestDTO request) {
        if (exception.getCode() == 409) {
            return handleConflictOnCreateUser(exception, email);
        }
        if (exception.getCode() == 401) {
            LOG.debug("Got 401, attempting token refresh and retry");
            return retryWithFreshToken(() -> {
                try {
                    LOG.debug("Retrying Claims Service call with fresh token");
                    var retryResponse = usersApi.createUser(request);
                    LOG.debug("Retry succeeded");
                    return mapToUserRecord(retryResponse);
                } catch (ApiException retryException) {
                    LOG.debug("Retry also failed with status {}, body={}", retryException.getCode(), retryException.getResponseBody());
                    throw new IllegalStateException("Claims Service call failed after token refresh: " + retryException.getMessage(), retryException);
                }
            });
        }
        throw new IllegalStateException("Claims Service call failed: " + exception.getMessage(), exception);
    }

    private UserRecord handleConflictOnCreateUser(ApiException exception, String email) {
        // User already exists by email — parse the existing user from the response body
        // This handles login auto-provisioning when UUID differs from what's in DB.
        // Signup never reaches here because Keycloak rejects duplicate emails first.
        String responseBody = exception.getResponseBody();
        if (responseBody != null && !responseBody.isBlank()) {
            try {
                var existing = objectMapper.readValue(responseBody, UserResponseDTO.class);
                LOG.debug("User already exists by email, returning existing user userId={}", existing.getUserId());
                return mapToUserRecord(existing);
            } catch (com.fasterxml.jackson.core.JsonProcessingException parseEx) {
                LOG.warn("409 from claims service but could not parse body", parseEx);
                var ex = new UserAlreadyExistsException(email, exception);
                ex.addSuppressed(parseEx);
                throw ex;
            }
        }
        throw new UserAlreadyExistsException(email, exception);
    }

    @Override
    public UserRecord getUser(UUID userId) {
        setServiceAccountBearerToken();

        try {
            var response = usersApi.getUser(userId);
            return mapToUserRecord(response);
        } catch (ApiException exception) {
            if (exception.getCode() == 401) {
                return retryWithFreshToken(() -> {
                    try {
                        var retryResponse = usersApi.getUser(userId);
                        return mapToUserRecord(retryResponse);
                    } catch (ApiException retryException) {
                        throw new IllegalStateException("Claims Service call failed after token refresh: " + retryException.getMessage(), retryException);
                    }
                });
            }
            throw new IllegalStateException("Claims Service call failed: " + exception.getMessage(), exception);
        }
    }

    @Override
    public ClaimRecord submitClaim(String userJwtToken, LocalDate incidentDate, String incidentLocation,
                                   String description, BigDecimal claimAmount) {
        setUserBearerToken(userJwtToken);

        var request = new CreateClaimRequestDTO();
        request.setIncidentDate(incidentDate);
        request.setIncidentLocation(incidentLocation);
        request.setDescription(description);
        request.setClaimAmount(claimAmount.doubleValue());

        try {
            var response = claimsApi.createClaim(request);
            return mapToClaimRecord(response);
        } catch (ApiException exception) {
            throw new IllegalStateException("Claims Service call failed: " + exception.getMessage(), exception);
        }
    }

    @Override
    public List<ClaimSummaryRecord> listClaims(String userJwtToken, UUID userId) {
        setUserBearerToken(userJwtToken);

        try {
            var response = claimsApi.listClaims();
            return response.stream()
                    .map(this::mapToClaimSummaryRecord)
                    .toList();
        } catch (ApiException exception) {
            throw new IllegalStateException("Claims Service call failed: " + exception.getMessage(), exception);
        }
    }

    @Override
    public ClaimRecord getClaim(String userJwtToken, UUID claimId) {
        setUserBearerToken(userJwtToken);

        try {
            var response = claimsApi.getClaim(claimId);
            return mapToClaimRecord(response);
        } catch (ApiException exception) {
            if (exception.getCode() == 403) {
                throw new IllegalStateException("Forbidden: not authorized to access this claim", exception);
            }
            if (exception.getCode() == 404) {
                throw new IllegalStateException("Claim not found: " + claimId, exception);
            }
            throw new IllegalStateException("Claims Service call failed: " + exception.getMessage(), exception);
        }
    }

    private ClaimRecord mapToClaimRecord(ClaimResponseDTO response) {
        return new ClaimRecord(
                response.getClaimId(),
                response.getUserId(),
                response.getIncidentDate(),
                response.getIncidentLocation(),
                response.getDescription(),
                BigDecimal.valueOf(response.getClaimAmount()),
                response.getStatus().getValue(),
                response.getCreatedAt(),
                response.getUpdatedAt()
        );
    }

    private ClaimSummaryRecord mapToClaimSummaryRecord(ClaimSummaryResponseDTO response) {
        return new ClaimSummaryRecord(
                response.getClaimId(),
                response.getIncidentDate(),
                response.getDescription(),
                BigDecimal.valueOf(response.getClaimAmount()),
                response.getStatus().getValue(),
                response.getCreatedAt()
        );
    }

    private void setServiceAccountBearerToken() {
        String token = serviceAccountTokenProvider.getServiceAccountToken();
        LOG.debug("Setting service account token (length={})", token.length());
        apiClient.setBearerToken(token);
    }

    private void setUserBearerToken(String userJwtToken) {
        apiClient.setBearerToken(userJwtToken);
    }

    private <T> T retryWithFreshToken(java.util.function.Supplier<T> operation) {
        String freshToken = serviceAccountTokenProvider.forceRefreshToken();
        apiClient.setBearerToken(freshToken);
        return operation.get();
    }

    private UserRecord mapToUserRecord(UserResponseDTO response) {
        return new UserRecord(
                response.getUserId(),
                response.getEmail(),
                response.getName(),
                response.getRole().getValue(),
                response.getCreatedAt() != null ? response.getCreatedAt() : OffsetDateTime.now()
        );
    }
}
