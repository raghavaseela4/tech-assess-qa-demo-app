package com.example.demo.user.adapter.out.persistence;

import com.example.demo.user.domain.UserRole;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true) // JPA Entity: Equality by business key (email) not ID
public class UserJpaEntity {

    @Id
    @Column(name = "user_id", nullable = false)
    private UUID id;

    @EqualsAndHashCode.Include // Business key for JPA entity - email is natural identifier
    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 50)
    private UserRole role;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
