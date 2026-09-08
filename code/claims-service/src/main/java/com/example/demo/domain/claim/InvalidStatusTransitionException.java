package com.example.demo.domain.claim;

import com.example.demo.domain.DomainException;

/**
 * Exception thrown when an invalid claim status transition is attempted.
 * Status transitions follow business rules defined in the ClaimStatus enum.
 */
public class InvalidStatusTransitionException extends DomainException {

    public InvalidStatusTransitionException(String message) {
        super(message);
    }
}
