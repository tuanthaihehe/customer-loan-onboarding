package com.f88.loanonboarding.dto.response.asset;

import java.math.BigDecimal;
import java.time.LocalDate;

public record VehicleMarketPriceResponse(
        String vehicleVariant,
        String vehicleVariantName,
        BigDecimal marketValue,
        String currencyCode,
        String priceSource,
        LocalDate effectiveFrom,
        LocalDate effectiveTo
) {
}
