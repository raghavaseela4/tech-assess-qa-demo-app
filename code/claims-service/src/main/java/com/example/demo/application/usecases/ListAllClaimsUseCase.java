package com.example.demo.application.usecases;

import com.example.demo.domain.claim.Claim;
import com.example.demo.domain.claim.ClaimRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Use case for listing all claims with optional filtering.
 * Admin-only operation - returns claims across all users.
 * NO RBAC logic here (enforced at adapter layer).
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ListAllClaimsUseCase {

    private final ClaimRepository claimRepository;

    /**
     * Executes the list all claims query with optional filters.
     *
     * @param query the query with optional status and userId filters
     * @return list of claims sorted by createdAt DESC
     */
    public List<Claim> execute(ListAllClaimsQuery query) {
        if (query.statusFilter().isPresent() && query.userIdFilter().isPresent()) {
            return claimRepository.findAllByStatusAndUserId(
                query.statusFilter().get(),
                query.userIdFilter().get()
            );
        } else if (query.statusFilter().isPresent()) {
            return claimRepository.findAllByStatus(query.statusFilter().get());
        } else if (query.userIdFilter().isPresent()) {
            return claimRepository.findByUserId(query.userIdFilter().get());
        } else {
            return claimRepository.findAll();
        }
    }
}
