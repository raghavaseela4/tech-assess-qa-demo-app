package com.example.demo.adapter.in.web;

import com.example.bff.adapter.in.web.generated.model.ClaimResponseDTO;
import com.example.bff.adapter.in.web.generated.model.ClaimStatusDTO;
import com.example.bff.adapter.in.web.generated.model.ClaimSummaryResponseDTO;
import com.example.demo.application.auth.ClaimsServicePort;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.math.BigDecimal;

@Mapper(componentModel = "spring")
public interface ClaimResponseMapper {

    @Mapping(target = "claimAmount", source = "claimAmount", qualifiedByName = "bigDecimalToDouble")
    @Mapping(target = "status", source = "status", qualifiedByName = "toStatusDTO")
    ClaimResponseDTO toResponse(ClaimsServicePort.ClaimRecord claim);

    @Mapping(target = "claimAmount", source = "claimAmount", qualifiedByName = "bigDecimalToDouble")
    @Mapping(target = "status", source = "status", qualifiedByName = "toStatusDTO")
    ClaimSummaryResponseDTO toSummaryResponse(ClaimsServicePort.ClaimSummaryRecord claim);

    @Named("bigDecimalToDouble")
    default Double bigDecimalToDouble(BigDecimal value) {
        return value == null ? null : value.doubleValue();
    }

    @Named("toStatusDTO")
    default ClaimStatusDTO toStatusDTO(String status) {
        return ClaimStatusDTO.fromValue(status);
    }
}
