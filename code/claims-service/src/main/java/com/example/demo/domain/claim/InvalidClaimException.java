package com.example.demo.domain.claim;

import com.example.demo.domain.DomainException;

/**
 * Exception thrown when claim data violates business rules or constraints.
 * This includes validation errors for claim amount, dates, descriptions, etc.
 */
public class InvalidClaimException extends DomainException {

    public InvalidClaimException(String message) {
        super(message);
    }
}
