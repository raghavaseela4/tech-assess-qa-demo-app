package com.example.demo.adapter.out.persistence;

import com.example.demo.domain.claim.Claim;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ClaimPersistenceMapper {

    ClaimEntity toEntity(Claim claim);

    default Claim toDomain(ClaimEntity entity) {
        if (entity == null) {
            return null;
        }
        return Claim.reconstitute(
                entity.getClaimId(),
                entity.getUserId(),
                entity.getIncidentDate(),
                entity.getIncidentLocation(),
                entity.getDescription(),
                entity.getClaimAmount(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
