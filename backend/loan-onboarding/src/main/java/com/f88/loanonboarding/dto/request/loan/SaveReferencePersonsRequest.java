package com.f88.loanonboarding.dto.request.loan;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public record SaveReferencePersonsRequest(
        @Valid
        @NotEmpty(message = "Danh sach nguoi tham chieu la bat buoc")
        @Size(min = 3, message = "Can toi thieu 3 nguoi tham chieu")
        List<ReferencePersonRequest> referencePersons
) {
}
