package com.f88.loanonboarding.dto.response.loan;

import java.util.List;

public record ReferencePersonsResponse(
        String applicationCode,
        int totalReferencePersons,
        List<ReferencePersonResponse> referencePersons
) {
}
