package com.example.demo.application.auth;

/**
 * Exception thrown when an external service (Keycloak, Claims Service) fails.
 * Infrastructure layer exception for external system failures.
 */
public class ExternalServiceException extends RuntimeException {

    private final String serviceName;

    public ExternalServiceException(String serviceName, String message) {
        super(String.format("External service '%s' failed: %s", serviceName, message));
        this.serviceName = serviceName;
    }

    public ExternalServiceException(String serviceName, String message, Throwable cause) {
        super(String.format("External service '%s' failed: %s", serviceName, message), cause);
        this.serviceName = serviceName;
    }

    public String getServiceName() {
        return serviceName;
    }
}
