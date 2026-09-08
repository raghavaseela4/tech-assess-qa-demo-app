package com.example.demo.adapter.in.web;

import com.example.claims.adapter.in.web.generated.UsersApi;
import com.example.claims.adapter.in.web.generated.model.CreateUserRequestDTO;
import com.example.claims.adapter.in.web.generated.model.UserResponseDTO;
import com.example.claims.adapter.in.web.generated.model.UserRoleDTO;
import com.example.demo.user.domain.Email;
import com.example.demo.user.domain.User;
import com.example.demo.user.domain.UserId;
import com.example.demo.user.domain.UserRepository;
import com.example.demo.user.domain.UserRole;
import com.example.demo.user.adapter.out.persistence.UserJpaRepository;
import com.example.demo.application.exceptions.UserNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@RestController
public class UserController implements UsersApi {

    private static final Logger LOG = LoggerFactory.getLogger(UserController.class);

    private final UserRepository userRepository;
    private final UserJpaRepository userJpaRepository;

    public UserController(UserRepository userRepository, UserJpaRepository userJpaRepository) {
        this.userRepository = userRepository;
        this.userJpaRepository = userJpaRepository;
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponseDTO>> listAllUsers() {
        List<UserResponseDTO> users = userRepository.findAll().stream()
                .map(this::toUserResponse)
                .toList();
        return ResponseEntity.ok(users);
    }

    @Override
    public ResponseEntity<UserResponseDTO> createUser(CreateUserRequestDTO request) {
        // Use userId from request (passed by BFF, which gets it from Keycloak)
        // Note: BFF calls this with service account token, so JWT subject != new user ID
        var userId = UserId.of(request.getUserId());
        var email = Email.of(request.getEmail());

        // If email already exists, handle idempotent provisioning
        var existing = userRepository.findByEmail(email);
        if (existing.isPresent()) {
            var existingUser = existing.get();
            if (existingUser.getUserId().equals(userId)) {
                // Same UUID — true idempotent call
                return ResponseEntity.status(HttpStatus.CONFLICT).body(toUserResponse(existingUser));
            }
            // UUID changed (e.g. Keycloak was recreated) — update to new Keycloak ID
            LOG.info("User email {} exists with old ID {}, updating to new Keycloak ID {}",
                    email.value(), existingUser.getUserId().value(), userId.value());
            userJpaRepository.updateUserId(existingUser.getUserId().value(), userId.value());
            userJpaRepository.flush();
            var updated = userJpaRepository.findById(userId.value())
                    .orElseThrow(() -> new IllegalStateException("User ID update failed"));
            return ResponseEntity.status(HttpStatus.CREATED).body(toJpaUserResponse(updated));
        }

        var createdAt = Instant.now();

        var user = new User(
                userId,
                request.getName(),
                email,
                UserRole.valueOf(request.getRole().getValue()),
                createdAt
        );

        userRepository.save(user);

        var response = new UserResponseDTO();
        response.setUserId(userId.value());
        response.setEmail(user.getEmail().value());
        response.setName(user.getName());
        response.setRole(UserRoleDTO.fromValue(user.getRole().name()));
        response.setCreatedAt(OffsetDateTime.ofInstant(createdAt, ZoneOffset.UTC));

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Override
    public ResponseEntity<UserResponseDTO> getUser(UUID userId) {
        var user = userRepository.findById(UserId.of(userId))
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        return ResponseEntity.ok(toUserResponse(user));
    }

    private UserResponseDTO toUserResponse(User user) {
        var response = new UserResponseDTO();
        response.setUserId(user.getUserId().value());
        response.setEmail(user.getEmail().value());
        response.setName(user.getName());
        response.setRole(UserRoleDTO.fromValue(user.getRole().name()));
        response.setCreatedAt(OffsetDateTime.ofInstant(user.getCreatedAt(), ZoneOffset.UTC));
        return response;
    }

    private UserResponseDTO toJpaUserResponse(com.example.demo.user.adapter.out.persistence.UserJpaEntity entity) {
        var response = new UserResponseDTO();
        response.setUserId(entity.getId());
        response.setEmail(entity.getEmail());
        response.setName(entity.getName());
        response.setRole(UserRoleDTO.fromValue(entity.getRole().name()));
        response.setCreatedAt(OffsetDateTime.ofInstant(entity.getCreatedAt(), ZoneOffset.UTC));
        return response;
    }
}
