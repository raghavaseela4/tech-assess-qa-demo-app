package com.example.demo.application.auth;

import java.util.Objects;

public record LoginCommand(String email, String password) {
    public LoginCommand {
        Objects.requireNonNull(email, "Email must not be null");
        Objects.requireNonNull(password, "Password must not be null");
    }
}
