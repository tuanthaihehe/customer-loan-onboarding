package com.f88.loanonboarding.dto.response.asset;

import java.time.LocalDate;

import com.f88.loanonboarding.enums.AssetType;

public record AssetLegalInfoResponse(
        String applicationCode,
        String assetCode,
        AssetType assetType,
        String licensePlate,
        String brand,
        String model,
        String vehicleVariant,
        Integer manufactureYear,
        String vehicleColor,
        String frameNumber,
        String engineNumber,
        String registrationNumber,
        LocalDate registrationIssueDate
) {
}
