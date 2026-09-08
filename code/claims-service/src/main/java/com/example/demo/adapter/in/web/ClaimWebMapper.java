package com.example.demo.adapter.in.web;

import com.example.claims.adapter.in.web.generated.model.ClaimResponseDTO;
import com.example.claims.adapter.in.web.generated.model.ClaimStatusDTO;
import com.example.claims.adapter.in.web.generated.model.ClaimSummaryResponseDTO;
import com.example.demo.domain.claim.Claim;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Mapper(componentModel = "spring")
public interface ClaimWebMapper {

    @Mapping(target = "claimAmount", expression = "java(claim.getClaimAmount().doubleValue())")
    @Mapping(target = "status", source = "status", qualifiedByName = "toStatusDTO")
    @Mapping(target = "createdAt", source = "createdAt", qualifiedByName = "toOffsetDateTime")
    @Mapping(target = "updatedAt", source = "updatedAt", qualifiedByName = "toOffsetDateTime")
    ClaimResponseDTO toResponse(Claim claim);

    @Mapping(target = "claimAmount", expression = "java(claim.getClaimAmount().doubleValue())")
    @Mapping(target = "description", source = "claim", qualifiedByName = "truncateDescription")
    @Mapping(target = "status", source = "status", qualifiedByName = "toStatusDTO")
    @Mapping(target = "createdAt", source = "createdAt", qualifiedByName = "toOffsetDateTime")
    ClaimSummaryResponseDTO toSummaryResponse(Claim claim);

    @Named("toStatusDTO")
    default ClaimStatusDTO toStatusDTO(com.example.demo.domain.claim.ClaimStatus status) {
        return ClaimStatusDTO.fromValue(status.name());
    }

    @Named("toOffsetDateTime")
    default OffsetDateTime toOffsetDateTime(Instant instant) {
        return instant == null ? null : OffsetDateTime.ofInstant(instant, ZoneOffset.UTC);
    }

    @Named("truncateDescription")
    default String truncateDescription(Claim claim) {
        String description = claim.getDescription();
        return description.length() > 100 ? description.substring(0, 100) : description;
    }
}
