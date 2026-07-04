package com.f88.loanonboarding.dto.request.loan;

import java.math.BigDecimal;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;

public record FinalLoanOfferPreviewRequest(
        @Positive(message = "Số tiền vay cuối cùng phải lớn hơn 0")
        BigDecimal requestedAmount,

        @Min(value = 1, message = "Kỳ hạn vay phải từ 1 tháng trở lên")
        Integer loanTermMonths,

        String paymentMethod,

        @Min(value = 1, message = "Ngày thanh toán hàng tháng phải từ 1 đến 28")
        @Max(value = 28, message = "Ngày thanh toán hàng tháng phải từ 1 đến 28")
        Integer monthlyPaymentDay,

        String processingBranch,

        @Min(value = 1, message = "Số gói vay cần trả về phải lớn hơn 0")
        @Max(value = 10, message = "Số gói vay cần trả về không được vượt quá 10")
        Integer limit
) {
}
