package com.example.demo.user.domain;

public enum UserRole {

    CLAIMANT("Claimant"),
    ADMIN("Admin");

    private final String displayName;

    UserRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
