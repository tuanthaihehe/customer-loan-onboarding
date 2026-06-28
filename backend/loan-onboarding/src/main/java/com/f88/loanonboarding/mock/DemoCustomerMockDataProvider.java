package com.f88.loanonboarding.mock;

import org.springframework.stereotype.Component;

import com.f88.loanonboarding.dto.request.customer.CustomerLookupRequest;
import com.f88.loanonboarding.dto.response.customer.CustomerLookupResponse;
import com.f88.loanonboarding.dto.response.customer.MatchedCustomerResponse;

@Component
public class DemoCustomerMockDataProvider {

    public static final String DEMO_CUSTOMER_CODE = "CUS-000001";

    public CustomerLookupResponse eligibleCustomer(CustomerLookupRequest request) {
        MatchedCustomerResponse matchedCustomer = new MatchedCustomerResponse(
                request.fullName(),
                request.dateOfBirth(),
                request.identifierNumber(),
                request.phoneNumber()
        );

        return new CustomerLookupResponse(
                true,
                DEMO_CUSTOMER_CODE,
                "CUS_PRE",
                "ELIGIBLE",
                "ALLOWED",
                matchedCustomer,
                null
        );
    }
}
