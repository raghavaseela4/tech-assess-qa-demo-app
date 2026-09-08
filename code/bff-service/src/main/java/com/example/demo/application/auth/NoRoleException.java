package com.example.demo.application.auth;

public class NoRoleException extends RuntimeException {

    public NoRoleException(String message) {
        super(message);
    }
}
