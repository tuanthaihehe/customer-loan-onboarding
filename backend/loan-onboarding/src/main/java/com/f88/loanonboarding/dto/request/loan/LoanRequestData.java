package com.f88.loanonboarding.dto.request.loan;

import java.math.BigDecimal;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record LoanRequestData(
        @NotBlank(message = "Mục đích vay là bắt buộc")
        String loanPurpose,

        @NotNull(message = "Số tiền mong muốn vay là bắt buộc")
        @Positive(message = "Số tiền mong muốn vay phải lớn hơn 0")
        BigDecimal requestedAmount,

        @NotNull(message = "Kỳ hạn vay là bắt buộc")
        @Min(value = 1, message = "Kỳ hạn vay phải từ 1 tháng trở lên")
        Integer requestedTenure
) {
}
