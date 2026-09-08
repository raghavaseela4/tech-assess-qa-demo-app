package com.example.demo.adapter.in.web;

import com.example.bff.adapter.in.web.generated.ClaimsApi;
import com.example.bff.adapter.in.web.generated.model.ClaimResponseDTO;
import com.example.bff.adapter.in.web.generated.model.ClaimSummaryResponseDTO;
import com.example.bff.adapter.in.web.generated.model.SubmitClaimRequestDTO;
import com.example.demo.application.auth.AuthenticationFacade;
import com.example.demo.application.auth.ClaimsServicePort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
public class ClaimsController implements ClaimsApi {

    private final AuthenticationFacade authenticationFacade;
    private final ClaimsServicePort claimsServicePort;
    private final ClaimResponseMapper claimResponseMapper;

    public ClaimsController(AuthenticationFacade authenticationFacade,
                            ClaimsServicePort claimsServicePort,
                            ClaimResponseMapper claimResponseMapper) {
        this.authenticationFacade = authenticationFacade;
        this.claimsServicePort = claimsServicePort;
        this.claimResponseMapper = claimResponseMapper;
    }

    @Override
    public ResponseEntity<ClaimResponseDTO> submitClaim(SubmitClaimRequestDTO request) {
        String jwtToken = authenticationFacade.getJwtTokenValue();

        ClaimsServicePort.ClaimRecord claim = claimsServicePort.submitClaim(
                jwtToken,
                request.getIncidentDate(),
                request.getIncidentLocation(),
                request.getDescription(),
                BigDecimal.valueOf(request.getClaimAmount())
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(claimResponseMapper.toResponse(claim));
    }

    @Override
    public ResponseEntity<List<ClaimSummaryResponseDTO>> listClaims() {
        String jwtToken = authenticationFacade.getJwtTokenValue();
        UUID userId = authenticationFacade.getCurrentUserId();

        List<ClaimsServicePort.ClaimSummaryRecord> claims = claimsServicePort.listClaims(jwtToken, userId);

        List<ClaimSummaryResponseDTO> response = claims.stream()
                .map(claimResponseMapper::toSummaryResponse)
                .toList();

        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<ClaimResponseDTO> getClaim(UUID claimId) {
        String jwtToken = authenticationFacade.getJwtTokenValue();

        ClaimsServicePort.ClaimRecord claim = claimsServicePort.getClaim(jwtToken, claimId);

        return ResponseEntity.ok(claimResponseMapper.toResponse(claim));
    }
}
