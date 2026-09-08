package com.example.demo.user.domain;

import com.example.demo.domain.DomainException;

/**
 * Exception thrown when a user attempts an operation they are not authorized to perform.
 * This represents a domain-level authorization violation, such as a non-admin user
 * attempting to perform admin-only operations.
 */
public class UnauthorizedException extends DomainException {

    public UnauthorizedException(String message) {
        super(message);
    }
}
