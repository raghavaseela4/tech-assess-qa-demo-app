package com.example.demo.adapter.in.web;

import com.example.bff.adapter.in.web.generated.AdminDashboardApi;
import com.example.bff.adapter.in.web.generated.model.ClaimResponseDTO;
import com.example.bff.adapter.in.web.generated.model.ClaimStatusDTO;
import com.example.bff.adapter.in.web.generated.model.DashboardStatsResponseDTO;
import com.example.bff.adapter.in.web.generated.model.UpdateClaimStatusRequestDTO;
import com.example.bff.adapter.in.web.generated.model.UserResponseDTO;
import com.example.bff.adapter.out.claims.ApiClient;
import com.example.bff.adapter.out.claims.ApiException;
import com.example.demo.application.auth.ServiceAccountTokenProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
public class AdminController implements AdminDashboardApi {

    private final com.example.bff.adapter.out.claims.generated.DashboardApi claimsDashboardApi;
    private final com.example.bff.adapter.out.claims.generated.AdminClaimsApi claimsAdminClaimsApi;
    private final com.example.bff.adapter.out.claims.generated.UsersApi claimsUsersApi;
    private final ApiClient apiClient;
    private final ServiceAccountTokenProvider serviceAccountTokenProvider;
    private final AdminClaimResponseMapper adminClaimResponseMapper;

    public AdminController(
            @Value("${claims-service.url}") String claimsServiceUrl,
            ServiceAccountTokenProvider serviceAccountTokenProvider,
            AdminClaimResponseMapper adminClaimResponseMapper) {
        this.apiClient = new ApiClient();
        this.apiClient.setBasePath(claimsServiceUrl);
        this.claimsDashboardApi = new com.example.bff.adapter.out.claims.generated.DashboardApi(apiClient);
        this.claimsAdminClaimsApi = new com.example.bff.adapter.out.claims.generated.AdminClaimsApi(apiClient);
        this.claimsUsersApi = new com.example.bff.adapter.out.claims.generated.UsersApi(apiClient);
        this.serviceAccountTokenProvider = serviceAccountTokenProvider;
        this.adminClaimResponseMapper = adminClaimResponseMapper;
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DashboardStatsResponseDTO> getDashboardStats(Optional<Boolean> bypassCache) {
        setServiceAccountBearerToken();
        try {
            var claimsResponse = claimsDashboardApi.getDashboardStats(bypassCache.orElse(false));
            var response = new DashboardStatsResponseDTO()
                    .totalUsers(claimsResponse.getTotalUsers())
                    .totalClaims(claimsResponse.getTotalClaims())
                    .claimsByStatus(claimsResponse.getClaimsByStatus())
                    .cacheHit(claimsResponse.getCacheHit())
                    .queryTimeMs(claimsResponse.getQueryTimeMs());
            return ResponseEntity.ok(response);
        } catch (ApiException exception) {
            if (exception.getCode() == 403) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            throw new IllegalStateException("Claims Service call failed: " + exception.getMessage(), exception);
        }
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> flushDashboardCache() {
        setServiceAccountBearerToken();
        try {
            claimsDashboardApi.flushDashboardCache();
            return ResponseEntity.noContent().build();
        } catch (ApiException exception) {
            if (exception.getCode() == 403) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            throw new IllegalStateException("Claims Service call failed: " + exception.getMessage(), exception);
        }
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponseDTO>> listAllUsers() {
        setUserBearerToken();
        try {
            var claimsResponse = claimsUsersApi.listAllUsers();
            var response = claimsResponse.stream()
                    .map(user -> new UserResponseDTO()
                            .userId(user.getUserId())
                            .email(user.getEmail())
                            .name(user.getName())
                            .role(com.example.bff.adapter.in.web.generated.model.UserRoleDTO.fromValue(user.getRole().getValue()))
                            .createdAt(user.getCreatedAt()))
                    .toList();
            return ResponseEntity.ok(response);
        } catch (ApiException exception) {
            if (exception.getCode() == 403) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            throw new IllegalStateException("Claims Service call failed: " + exception.getMessage(), exception);
        }
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ClaimResponseDTO>> listAllClaims(Optional<ClaimStatusDTO> status, Optional<UUID> userId) {
        setServiceAccountBearerToken();
        try {
            com.example.bff.adapter.out.claims.generated.model.ClaimStatusDTO claimsStatus =
                    status.map(s -> com.example.bff.adapter.out.claims.generated.model.ClaimStatusDTO.fromValue(s.getValue()))
                            .orElse(null);

            var claimsResponse = claimsAdminClaimsApi.listAllClaims(claimsStatus, userId.orElse(null));
            return ResponseEntity.ok(adminClaimResponseMapper.toClaimResponseList(claimsResponse));
        } catch (ApiException exception) {
            if (exception.getCode() == 403) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            throw new IllegalStateException("Claims Service call failed: " + exception.getMessage(), exception);
        }
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ClaimResponseDTO> updateClaimStatus(UUID claimId, UpdateClaimStatusRequestDTO updateClaimStatusRequestDTO) {
        setUserBearerToken();
        try {
            var claimsRequest = new com.example.bff.adapter.out.claims.generated.model.UpdateClaimStatusRequestDTO()
                    .newStatus(com.example.bff.adapter.out.claims.generated.model.ClaimStatusDTO.fromValue(
                            updateClaimStatusRequestDTO.getNewStatus().getValue()));

            var claimsResponse = claimsAdminClaimsApi.updateClaimStatus(claimId, claimsRequest);
            return ResponseEntity.ok(adminClaimResponseMapper.toClaimResponse(claimsResponse));
        } catch (ApiException exception) {
            if (exception.getCode() == 400) {
                return ResponseEntity.badRequest().build();
            }
            if (exception.getCode() == 403) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            if (exception.getCode() == 404) {
                return ResponseEntity.notFound().build();
            }
            throw new IllegalStateException("Claims Service call failed: " + exception.getMessage(), exception);
        }
    }

    private void setServiceAccountBearerToken() {
        String token = serviceAccountTokenProvider.getServiceAccountToken();
        apiClient.setBearerToken(token);
    }

    private void setUserBearerToken() {
        var authentication = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof org.springframework.security.oauth2.jwt.Jwt jwt) {
            apiClient.setBearerToken(jwt.getTokenValue());
        } else {
            throw new IllegalStateException("No user JWT token found in security context");
        }
    }
}
