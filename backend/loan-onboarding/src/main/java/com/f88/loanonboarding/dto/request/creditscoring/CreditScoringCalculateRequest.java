package com.f88.loanonboarding.dto.request.creditscoring;

import java.math.BigDecimal;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record CreditScoringCalculateRequest(
        String ruleSetCode,

        @NotNull(message = "Thu nhập hàng tháng là bắt buộc")
        @PositiveOrZero(message = "Thu nhập hàng tháng không được âm")
        BigDecimal monthlyIncomeAmount,

        @NotNull(message = "Tuổi khách hàng là bắt buộc")
        @Min(value = 18, message = "Tuổi khách hàng phải từ 18 trở lên")
        Integer age,

        @NotNull(message = "Số người phụ thuộc là bắt buộc")
        @Min(value = 0, message = "Số người phụ thuộc không được âm")
        Integer dependentCount
) {
}
