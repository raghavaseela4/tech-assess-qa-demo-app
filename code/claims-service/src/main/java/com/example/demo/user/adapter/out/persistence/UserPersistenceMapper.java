package com.example.demo.user.adapter.out.persistence;

import com.example.demo.user.domain.Email;
import com.example.demo.user.domain.User;
import com.example.demo.user.domain.UserId;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface UserPersistenceMapper {

    @Mapping(target = "id", source = "userId.value")
    @Mapping(target = "email", source = "email.value")
    UserJpaEntity toJpaEntity(User user);

    default User toDomain(UserJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        return new User(
                UserId.of(entity.getId()),
                entity.getName(),
                Email.of(entity.getEmail()),
                entity.getRole(),
                entity.getCreatedAt()
        );
    }

    default UUID mapUserId(UserId userId) {
        return userId == null ? null : userId.value();
    }

    default String mapEmail(Email email) {
        return email == null ? null : email.value();
    }
}
