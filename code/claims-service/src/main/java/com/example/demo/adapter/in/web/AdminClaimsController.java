package com.example.demo.adapter.in.web;

import com.example.claims.adapter.in.web.generated.AdminClaimsApi;
import com.example.claims.adapter.in.web.generated.model.AdminClaimResponseDTO;
import com.example.claims.adapter.in.web.generated.model.ClaimResponseDTO;
import com.example.claims.adapter.in.web.generated.model.ClaimStatusDTO;
import com.example.claims.adapter.in.web.generated.model.UpdateClaimStatusRequestDTO;
import com.example.demo.application.usecases.ListAllClaimsQuery;
import com.example.demo.application.usecases.ListAllClaimsUseCase;
import com.example.demo.application.usecases.UpdateClaimStatusCommand;
import com.example.demo.application.usecases.UpdateClaimStatusUseCase;
import com.example.demo.domain.claim.Claim;
import com.example.demo.domain.claim.ClaimStatus;
import com.example.demo.user.domain.User;
import com.example.demo.user.domain.UserId;
import com.example.demo.user.domain.UserRepository;
import io.micrometer.tracing.Tracer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * REST controller for admin claims management endpoints.
 * Thin adapter that delegates to use cases and enriches responses with user data.
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class AdminClaimsController implements AdminClaimsApi {

    private final ListAllClaimsUseCase listAllClaimsUseCase;
    private final UpdateClaimStatusUseCase updateClaimStatusUseCase;
    private final UserRepository userRepository;
    private final ClaimWebMapper claimWebMapper;
    private final Tracer tracer;

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AdminClaimResponseDTO>> listAllClaims(
            Optional<ClaimStatusDTO> status,
            Optional<UUID> userId) {

        ListAllClaimsQuery query = buildQuery(status, userId);
        List<Claim> claims = listAllClaimsUseCase.execute(query);

        List<AdminClaimResponseDTO> response = claims.stream()
                .map(this::toAdminClaimResponse)
                .toList();

        return ResponseEntity.ok(response);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ClaimResponseDTO> updateClaimStatus(
            UUID claimId,
            UpdateClaimStatusRequestDTO requestDTO) {

        ClaimStatus newStatus = ClaimStatus.valueOf(requestDTO.getNewStatus().getValue());
        UUID adminUserId = extractAdminUserId();

        String correlationId = MDC.get("correlationId");
        MDC.put("userId", adminUserId.toString());
        MDC.put("claimId", claimId.toString());

        // Add span tags for distributed tracing
        var currentSpan = tracer.currentSpan();
        if (currentSpan != null) {
            currentSpan.tag("userId", adminUserId.toString());
            currentSpan.tag("claimId", claimId.toString());
            currentSpan.tag("role", "ADMIN");
            if (correlationId != null) {
                currentSpan.tag("correlationId", correlationId);
            }
        }

        UpdateClaimStatusCommand command = new UpdateClaimStatusCommand(
                claimId,
                newStatus,
                adminUserId
        );

        Claim updatedClaim = updateClaimStatusUseCase.execute(command);
        return ResponseEntity.ok(claimWebMapper.toResponse(updatedClaim));
    }

    /**
     * Extracts the authenticated admin user ID from Spring Security context.
     *
     * @return UUID of the authenticated admin user
     * @throws IllegalStateException if no authenticated user found
     */
    private UUID extractAdminUserId() {
        var authentication = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            return UUID.fromString(jwt.getSubject());
        }
        throw new IllegalStateException("No authenticated user found in security context");
    }

    /**
     * Builds a domain query object from optional HTTP query parameters.
     *
     * @param status optional status filter from query string
     * @param userId optional userId filter from query string
     * @return ListAllClaimsQuery with appropriate filters applied
     */
    private ListAllClaimsQuery buildQuery(Optional<ClaimStatusDTO> status, Optional<UUID> userId) {
        Optional<ClaimStatus> statusFilter = status.map(s -> ClaimStatus.valueOf(s.getValue()));

        if (statusFilter.isPresent() && userId.isPresent()) {
            return ListAllClaimsQuery.byStatusAndUserId(statusFilter.get(), userId.get());
        } else if (statusFilter.isPresent()) {
            return ListAllClaimsQuery.byStatus(statusFilter.get());
        } else if (userId.isPresent()) {
            return ListAllClaimsQuery.byUserId(userId.get());
        } else {
            return ListAllClaimsQuery.all();
        }
    }

    /**
     * Maps a domain Claim to AdminClaimResponseDTO with enriched user data.
     * Fetches user information from UserRepository to include userName and userEmail.
     * Falls back to "Unknown User" if user not found (data integrity issue).
     *
     * @param claim the domain claim to map
     * @return AdminClaimResponseDTO with claim and user data
     */
    private AdminClaimResponseDTO toAdminClaimResponse(Claim claim) {
        AdminClaimResponseDTO response = new AdminClaimResponseDTO();
        response.setClaimId(claim.getClaimId());
        response.setUserId(claim.getUserId());
        response.setIncidentDate(claim.getIncidentDate());
        response.setIncidentLocation(claim.getIncidentLocation());
        response.setDescription(claim.getDescription());
        response.setClaimAmount(claim.getClaimAmount().doubleValue());
        response.setStatus(ClaimStatusDTO.fromValue(claim.getStatus().name()));
        response.setCreatedAt(claimWebMapper.toOffsetDateTime(claim.getCreatedAt()));
        response.setUpdatedAt(claimWebMapper.toOffsetDateTime(claim.getUpdatedAt()));

        Optional<User> user = userRepository.findById(UserId.of(claim.getUserId()));
        if (user.isPresent()) {
            response.setUserName(user.get().getName());
            response.setUserEmail(user.get().getEmail().value());
        } else {
            log.warn("User not found for claim {}: userId={} - data integrity issue",
                    claim.getClaimId(), claim.getUserId());
            response.setUserName("Unknown User");
            response.setUserEmail("unknown@example.com");
        }

        return response;
    }

}
