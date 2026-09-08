package com.example.demo.adapter.in.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;

@Component
public class ErrorTypeConstants {

    private final String baseUrl;

    public ErrorTypeConstants(@Value("${app.error-base-url}") String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public URI userExists() {
        return URI.create(baseUrl + "/user-exists");
    }

    public URI authFailed() {
        return URI.create(baseUrl + "/auth-failed");
    }

    public URI validationError() {
        return URI.create(baseUrl + "/validation-error");
    }

    public URI accessDenied() {
        return URI.create(baseUrl + "/access-denied");
    }

    public URI unauthorized() {
        return URI.create(baseUrl + "/unauthorized");
    }
}
