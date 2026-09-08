package com.example.demo.user.domain;

import java.util.Objects;

public record Email(String value) {

    public Email {
        Objects.requireNonNull(value, "Email must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("Email must not be blank");
        }
        // RFC 5322 simplified: require @ with domain having at least one dot and TLD
        if (!value.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new IllegalArgumentException("Invalid email format: " + value);
        }
    }

    public static Email of(String value) {
        return new Email(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
