package com.f88.loanonboarding.service;

import com.f88.loanonboarding.dto.request.customer.CustomerLookupRequest;
import com.f88.loanonboarding.dto.response.customer.CustomerLookupResponse;

public interface CustomerService {

    CustomerLookupResponse lookup(CustomerLookupRequest request);
}
