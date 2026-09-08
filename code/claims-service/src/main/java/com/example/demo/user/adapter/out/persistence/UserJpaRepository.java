package com.example.demo.user.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

public interface UserJpaRepository extends JpaRepository<UserJpaEntity, UUID> {

    Optional<UserJpaEntity> findByEmail(String email);

    boolean existsByEmail(String email);

    @Modifying
    @Transactional
    @Query("UPDATE UserJpaEntity u SET u.id = :newId WHERE u.id = :oldId")
    int updateUserId(UUID oldId, UUID newId);
}
