package com.example.demo.domain;

/**
 * Base class for all domain layer exceptions.
 * Domain exceptions represent business rule violations or domain-specific error conditions.
 */
public abstract class DomainException extends RuntimeException {

    protected DomainException(String message) {
        super(message);
    }

    protected DomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
