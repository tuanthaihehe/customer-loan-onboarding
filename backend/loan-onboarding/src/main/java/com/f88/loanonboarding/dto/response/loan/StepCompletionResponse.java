package com.f88.loanonboarding.dto.response.loan;

import java.util.List;

public record StepCompletionResponse(
        String applicationCode,
        String step,
        boolean completed,
        String nextStep,
        List<String> validationErrors
) {
}
