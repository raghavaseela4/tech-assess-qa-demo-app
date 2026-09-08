package com.example.demo.application.auth;

import java.util.Objects;
import java.util.regex.Pattern;

public record SignupCommand(String email, String password, String name) {
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[A-Z])(?=.*\\d).{8,}$"
    );

    public SignupCommand {
        Objects.requireNonNull(email, "Email must not be null");
        Objects.requireNonNull(password, "Password must not be null");
        Objects.requireNonNull(name, "Name must not be null");

        if (email.isBlank()) {
            throw new IllegalArgumentException("Email must not be blank");
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("Invalid email format");
        }

        if (password.isBlank()) {
            throw new IllegalArgumentException("Password must not be blank");
        }
        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            throw new IllegalArgumentException("Password must be at least 8 characters with 1 uppercase and 1 number");
        }

        if (name.isBlank()) {
            throw new IllegalArgumentException("Name must not be blank");
        }
    }
}
