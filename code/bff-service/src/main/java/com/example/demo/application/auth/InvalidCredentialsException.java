package com.example.demo.application.auth;

public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException() {
        super("Invalid email or password");
    }

    public InvalidCredentialsException(Throwable cause) {
        super("Invalid email or password", cause);
    }
}
