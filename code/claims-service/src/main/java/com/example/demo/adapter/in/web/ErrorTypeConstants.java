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

    public URI validationError() {
        return URI.create(baseUrl + "/validation-error");
    }

    public URI notFound() {
        return URI.create(baseUrl + "/not-found");
    }

    public URI forbidden() {
        return URI.create(baseUrl + "/forbidden");
    }
}
