package com.example.demo.domain.claim;

import com.example.demo.domain.DomainException;

import java.util.UUID;

/**
 * Exception thrown when a claim cannot be found by its identifier.
 * This represents a domain-level error condition where a requested claim does not exist
 * in the system or is not accessible.
 */
public class ClaimNotFoundException extends DomainException {

    public ClaimNotFoundException(UUID claimId) {
        super("Claim not found: " + claimId);
    }
}
