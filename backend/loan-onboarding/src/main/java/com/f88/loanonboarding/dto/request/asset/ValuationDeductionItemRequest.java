package com.f88.loanonboarding.dto.request.asset;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record ValuationDeductionItemRequest(
        @NotBlank(message = "Loại yếu tố giảm trừ là bắt buộc")
        String type,

        @NotNull(message = "Tỷ lệ giảm trừ là bắt buộc")
        @PositiveOrZero(message = "Tỷ lệ giảm trừ không được âm")
        BigDecimal rate
) {
}
