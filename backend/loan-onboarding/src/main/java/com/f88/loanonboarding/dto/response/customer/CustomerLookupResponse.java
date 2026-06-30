package com.f88.loanonboarding.dto.response.customer;

public record CustomerLookupResponse(
        boolean found,
        String customerCode,
        String customerState,
        String lookupStatus,
        String onboardingPermission,
        MatchedCustomerResponse matchedCustomer,
        String reasonCode
) {
}
