package com.example.demo.application.exceptions;

import java.util.UUID;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(UUID userId) {
        super("User not found with ID: " + userId);
    }

    public UserNotFoundException(String message) {
        super(message);
    }
}
