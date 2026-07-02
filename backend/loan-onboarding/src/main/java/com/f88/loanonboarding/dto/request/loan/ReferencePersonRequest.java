package com.f88.loanonboarding.dto.request.loan;

import jakarta.validation.constraints.NotBlank;

public record ReferencePersonRequest(
        @NotBlank(message = "Ho va ten nguoi tham chieu la bat buoc")
        String fullName,

        @NotBlank(message = "So dien thoai nguoi tham chieu la bat buoc")
        String phoneNumber,

        @NotBlank(message = "Moi quan he la bat buoc")
        String relationshipType,

        String address,

        String note
) {
}
