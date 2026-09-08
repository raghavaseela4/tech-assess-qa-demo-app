package com.example.demo.adapter.in.web;

import com.example.bff.adapter.in.web.generated.model.ClaimResponseDTO;
import com.example.bff.adapter.in.web.generated.model.ClaimStatusDTO;
import com.example.bff.adapter.out.claims.generated.model.AdminClaimResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

/**
 * Maps claims-service client DTOs to BFF response DTOs for admin claims management.
 */
@Mapper(componentModel = "spring")
public interface AdminClaimResponseMapper {

    @Mapping(target = "status", source = "status", qualifiedByName = "toBffClaimStatus")
    ClaimResponseDTO toClaimResponse(AdminClaimResponseDTO adminClaim);

    List<ClaimResponseDTO> toClaimResponseList(List<AdminClaimResponseDTO> adminClaims);

    @Mapping(target = "status", source = "status", qualifiedByName = "toBffClaimStatus")
    ClaimResponseDTO toClaimResponse(com.example.bff.adapter.out.claims.generated.model.ClaimResponseDTO claimsServiceResponse);

    @Named("toBffClaimStatus")
    default ClaimStatusDTO toBffClaimStatus(com.example.bff.adapter.out.claims.generated.model.ClaimStatusDTO status) {
        return status == null ? null : ClaimStatusDTO.fromValue(status.getValue());
    }
}
