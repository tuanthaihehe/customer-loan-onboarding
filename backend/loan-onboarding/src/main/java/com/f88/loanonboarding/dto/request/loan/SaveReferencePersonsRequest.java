package com.f88.loanonboarding.dto.request.loan;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public record SaveReferencePersonsRequest(
        @Valid
        @NotEmpty(message = "Danh sách người tham chiếu là bắt buộc")
        @Size(min = 3, message = "Cần tối thiểu 3 người tham chiếu")
        List<ReferencePersonRequest> referencePersons
) {
}
