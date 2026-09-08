package com.example.demo.user.domain;

import java.util.List;
import java.util.Optional;

public interface UserRepository {

    User save(User user);

    Optional<User> findById(UserId userId);

    Optional<User> findByEmail(Email email);

    boolean existsByEmail(Email email);

    List<User> findAll();

    long count();
}
