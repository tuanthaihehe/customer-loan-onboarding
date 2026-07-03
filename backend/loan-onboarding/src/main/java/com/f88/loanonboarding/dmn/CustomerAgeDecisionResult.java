package com.f88.loanonboarding.dmn;

public record CustomerAgeDecisionResult(
        boolean eligible,
        String reasonCode,
        String reasonMessage
) {
}
