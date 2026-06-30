package com.f88.loanonboarding.service;

import com.f88.loanonboarding.dto.request.customer.CustomerLookupRequest;
import com.f88.loanonboarding.dto.request.customer.CreateCustomerRequest;
import com.f88.loanonboarding.dto.response.customer.CreatedCustomerResponse;
import com.f88.loanonboarding.dto.response.customer.CustomerLookupResponse;

public interface CustomerService {

    CustomerLookupResponse lookup(CustomerLookupRequest request);

    CreatedCustomerResponse create(CreateCustomerRequest request);
}
