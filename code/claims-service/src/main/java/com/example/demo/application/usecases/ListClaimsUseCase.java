package com.example.demo.application.usecases;

import com.example.demo.domain.claim.Claim;
import com.example.demo.domain.claim.ClaimRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Query use case for listing all claims for a user.
 * Claims are sorted by creation date (newest first).
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ListClaimsUseCase {

    private final ClaimRepository claimRepository;

    /**
     * Executes the list claims query.
     *
     * @param query the query containing userId
     * @return list of claims belonging to the user, sorted by createdAt DESC
     */
    public List<Claim> execute(ListClaimsQuery query) {
        return claimRepository.findByUserId(query.userId());
    }
}
