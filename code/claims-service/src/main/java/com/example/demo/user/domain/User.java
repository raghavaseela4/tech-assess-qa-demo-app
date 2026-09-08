package com.example.demo.user.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true) // DDD Entity: Equality by identity (userId only)
public class User {

    @EqualsAndHashCode.Include // Entity identity - two users with same ID are equal even if data differs
    private final UserId userId;
    private String name;
    private Email email;
    private final UserRole role;
    private final Instant createdAt;
    private final List<DomainEvent> domainEvents = new ArrayList<>();

    public User(UserId userId, String name, Email email, UserRole role, Instant createdAt) {
        Objects.requireNonNull(userId, "UserId must not be null");
        Objects.requireNonNull(email, "Email must not be null");
        Objects.requireNonNull(role, "Role must not be null");
        Objects.requireNonNull(createdAt, "CreatedAt must not be null");
        validateName(name);
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.role = role;
        this.createdAt = createdAt;
    }

    public void changeName(String newName) {
        validateName(newName);
        this.name = newName;
    }

    public void updateEmail(Email newEmail) {
        Objects.requireNonNull(newEmail, "Email must not be null");
        this.email = newEmail;
    }

    public List<DomainEvent> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }

    private static void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name must not be blank");
        }
        if (name.length() > 255) {
            throw new IllegalArgumentException("Name must not exceed 255 characters");
        }
    }
}
