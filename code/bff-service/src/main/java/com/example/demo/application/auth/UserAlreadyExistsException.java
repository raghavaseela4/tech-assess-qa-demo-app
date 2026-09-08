package com.example.demo.application.auth;

public class UserAlreadyExistsException extends RuntimeException {

    private final String email;

    public UserAlreadyExistsException(String email) {
        super("User already exists with email: " + email);
        this.email = email;
    }

    public UserAlreadyExistsException(String email, Throwable cause) {
        super("User already exists with email: " + email, cause);
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
}
