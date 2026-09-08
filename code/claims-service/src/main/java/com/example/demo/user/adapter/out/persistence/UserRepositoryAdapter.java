package com.example.demo.user.adapter.out.persistence;

import com.example.demo.user.domain.Email;
import com.example.demo.user.domain.User;
import com.example.demo.user.domain.UserId;
import com.example.demo.user.domain.UserRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class UserRepositoryAdapter implements UserRepository {

    private final UserJpaRepository userJpaRepository;
    private final UserPersistenceMapper mapper;

    public UserRepositoryAdapter(UserJpaRepository userJpaRepository, UserPersistenceMapper mapper) {
        this.userJpaRepository = userJpaRepository;
        this.mapper = mapper;
    }

    @Override
    public User save(User user) {
        var entity = mapper.toJpaEntity(user);
        var saved = userJpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<User> findById(UserId userId) {
        return userJpaRepository.findById(userId.value())
                .map(mapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(Email email) {
        return userJpaRepository.findByEmail(email.value())
                .map(mapper::toDomain);
    }

    @Override
    public boolean existsByEmail(Email email) {
        return userJpaRepository.existsByEmail(email.value());
    }

    @Override
    public List<User> findAll() {
        return userJpaRepository.findAll().stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public long count() {
        return userJpaRepository.count();
    }
}
