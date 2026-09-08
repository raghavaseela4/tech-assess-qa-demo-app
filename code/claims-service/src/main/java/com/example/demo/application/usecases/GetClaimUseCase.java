package com.example.demo.application.usecases;

import com.example.demo.application.exceptions.UnauthorizedClaimAccessException;
import com.example.demo.domain.claim.Claim;
import com.example.demo.domain.claim.ClaimNotFoundException;
import com.example.demo.domain.claim.ClaimRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * Query use case for retrieving a single claim.
 * Enforces RBAC: users can only access their own claims.
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GetClaimUseCase {

    private final ClaimRepository claimRepository;

    /**
     * Executes the get claim query.
     *
     * @param query the query containing claimId and requesting userId
     * @return the claim if found and user is authorized
     * @throws ClaimNotFoundException if claim doesn't exist
     * @throws UnauthorizedClaimAccessException if user doesn't own the claim
     */
    public Claim execute(GetClaimQuery query) {
        Claim claim = claimRepository.findById(query.claimId())
                .orElseThrow(() -> new ClaimNotFoundException(query.claimId()));

        if (!claim.getUserId().equals(query.requestingUserId())) {
            throw new UnauthorizedClaimAccessException(query.requestingUserId(), query.claimId());
        }

        return claim;
    }
}
