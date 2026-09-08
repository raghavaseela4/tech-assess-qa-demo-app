package com.example.demo.adapter.in.web;

import com.example.claims.adapter.in.web.generated.model.ClaimResponseDTO;
import com.example.claims.adapter.in.web.generated.model.ClaimSummaryResponseDTO;
import com.example.claims.adapter.in.web.generated.model.CreateClaimRequestDTO;
import com.example.demo.application.usecases.CreateClaimCommand;
import com.example.demo.application.usecases.CreateClaimUseCase;
import com.example.demo.application.usecases.GetClaimQuery;
import com.example.demo.application.usecases.GetClaimUseCase;
import com.example.demo.application.usecases.ListClaimsQuery;
import com.example.demo.application.usecases.ListClaimsUseCase;
import com.example.demo.domain.claim.Claim;
import io.micrometer.tracing.Tracer;
import jakarta.validation.Valid;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/claims")
public class ClaimController {

    private final CreateClaimUseCase createClaimUseCase;
    private final GetClaimUseCase getClaimUseCase;
    private final ListClaimsUseCase listClaimsUseCase;
    private final ClaimWebMapper claimWebMapper;
    private final Tracer tracer;

    public ClaimController(CreateClaimUseCase createClaimUseCase,
                           GetClaimUseCase getClaimUseCase,
                           ListClaimsUseCase listClaimsUseCase,
                           ClaimWebMapper claimWebMapper,
                           Tracer tracer) {
        this.createClaimUseCase = createClaimUseCase;
        this.getClaimUseCase = getClaimUseCase;
        this.listClaimsUseCase = listClaimsUseCase;
        this.claimWebMapper = claimWebMapper;
        this.tracer = tracer;
    }

    @PostMapping
    public ResponseEntity<ClaimResponseDTO> createClaim(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreateClaimRequestDTO request) {

        UUID userId = UUID.fromString(jwt.getSubject());
        String correlationId = MDC.get("correlationId");
        MDC.put("userId", userId.toString());

        // Add span tags for distributed tracing
        var currentSpan = tracer.currentSpan();
        if (currentSpan != null) {
            currentSpan.tag("userId", userId.toString());
            if (correlationId != null) {
                currentSpan.tag("correlationId", correlationId);
            }
        }

        CreateClaimCommand command = new CreateClaimCommand(
                userId,
                request.getIncidentDate(),
                request.getIncidentLocation(),
                request.getDescription(),
                BigDecimal.valueOf(request.getClaimAmount())
        );

        Claim claim = createClaimUseCase.execute(command);
        MDC.put("claimId", claim.getClaimId().toString());

        // Add claimId to span after creation
        if (currentSpan != null) {
            currentSpan.tag("claimId", claim.getClaimId().toString());
        }

        URI location = URI.create("/api/claims/" + claim.getClaimId());
        return ResponseEntity.created(location).body(claimWebMapper.toResponse(claim));
    }

    @GetMapping("/{claimId}")
    public ResponseEntity<ClaimResponseDTO> getClaim(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID claimId) {

        UUID userId = UUID.fromString(jwt.getSubject());
        String correlationId = MDC.get("correlationId");
        MDC.put("userId", userId.toString());
        MDC.put("claimId", claimId.toString());

        // Add span tags for distributed tracing
        var currentSpan = tracer.currentSpan();
        if (currentSpan != null) {
            currentSpan.tag("userId", userId.toString());
            currentSpan.tag("claimId", claimId.toString());
            if (correlationId != null) {
                currentSpan.tag("correlationId", correlationId);
            }
        }

        Claim claim = getClaimUseCase.execute(new GetClaimQuery(claimId, userId));
        return ResponseEntity.ok(claimWebMapper.toResponse(claim));
    }

    @GetMapping
    public ResponseEntity<List<ClaimSummaryResponseDTO>> listClaims(
            @AuthenticationPrincipal Jwt jwt) {

        UUID userId = UUID.fromString(jwt.getSubject());
        String correlationId = MDC.get("correlationId");
        MDC.put("userId", userId.toString());

        // Add span tags for distributed tracing
        var currentSpan = tracer.currentSpan();
        if (currentSpan != null) {
            currentSpan.tag("userId", userId.toString());
            if (correlationId != null) {
                currentSpan.tag("correlationId", correlationId);
            }
        }

        List<Claim> claims = listClaimsUseCase.execute(new ListClaimsQuery(userId));

        List<ClaimSummaryResponseDTO> response = claims.stream()
                .map(claimWebMapper::toSummaryResponse)
                .toList();

        return ResponseEntity.ok(response);
    }
}
