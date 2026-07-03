package com.f88.loanonboarding.dto.request.loan;

import jakarta.validation.constraints.NotBlank;

public record ReferencePersonRequest(
        @NotBlank(message = "Họ và tên người tham chiếu là bắt buộc")
        String fullName,

        @NotBlank(message = "Số điện thoại người tham chiếu là bắt buộc")
        String phoneNumber,

        @NotBlank(message = "Mối quan hệ là bắt buộc")
        String relationshipType,

        String address,

        String note
) {
}
