package com.f88.loanonboarding.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.f88.loanonboarding.dto.request.customer.CustomerLookupRequest;
import com.f88.loanonboarding.dto.response.customer.CustomerLookupResponse;
import com.f88.loanonboarding.dto.response.customer.MatchedCustomerResponse;
import com.f88.loanonboarding.entity.Customer;
import com.f88.loanonboarding.repository.CustomerRepository;
import com.f88.loanonboarding.rule.RuleContext;
import com.f88.loanonboarding.rule.RuleEvaluationService;
import com.f88.loanonboarding.rule.customer.CustomerAgeRule;
import com.f88.loanonboarding.rule.customer.CustomerBlacklistRule;
import com.f88.loanonboarding.service.CustomerService;

@Service
public class CustomerServiceImpl implements CustomerService {

    private static final String STATUS_ACTIVE = "ACTIVE";

    private final CustomerRepository customerRepository;
    private final RuleEvaluationService ruleEvaluationService;

    public CustomerServiceImpl(CustomerRepository customerRepository, RuleEvaluationService ruleEvaluationService) {
        this.customerRepository = customerRepository;
        this.ruleEvaluationService = ruleEvaluationService;
    }

    @Override
    public CustomerLookupResponse lookup(CustomerLookupRequest request) {
        ruleEvaluationService.validateOrThrow(
                RuleContext.customer(null, request.dateOfBirth(), false),
                List.of(new CustomerBlacklistRule(), new CustomerAgeRule())
        );

        return customerRepository
                .findFirstByIdentityNumberOrPhoneNumberOrderByCustomerCodeAsc(
                        request.identifierNumber(),
                        request.phoneNumber()
                )
                .map(this::toLookupResponse)
                .orElseGet(() -> new CustomerLookupResponse(
                        false,
                        null,
                        null,
                        null,
                        "CREATE_NEW_CUSTOMER",
                        null,
                        "CUSTOMER_NOT_FOUND"
                ));
    }

    private CustomerLookupResponse toLookupResponse(Customer customer) {
        boolean eligible = STATUS_ACTIVE.equals(customer.getStatus());
        return new CustomerLookupResponse(
                true,
                customer.getCustomerCode(),
                customer.getStatus(),
                eligible ? "ELIGIBLE" : "NOT_ELIGIBLE",
                eligible ? "ALLOWED" : "BLOCKED",
                new MatchedCustomerResponse(
                        customer.getFullName(),
                        customer.getDateOfBirth(),
                        customer.getIdentityNumber(),
                        customer.getPhoneNumber()
                ),
                eligible ? null : "CUSTOMER_" + customer.getStatus()
        );
    }
}
