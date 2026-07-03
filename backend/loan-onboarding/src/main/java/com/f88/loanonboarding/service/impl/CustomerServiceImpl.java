package com.f88.loanonboarding.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.f88.loanonboarding.dto.request.customer.CustomerLookupRequest;
import com.f88.loanonboarding.dto.response.customer.CustomerLookupResponse;
import com.f88.loanonboarding.dto.response.customer.MatchedCustomerResponse;
import com.f88.loanonboarding.entity.Customer;
import com.f88.loanonboarding.enums.CustomerStatus;
import com.f88.loanonboarding.repository.CustomerRepository;
import com.f88.loanonboarding.rule.RuleContext;
import com.f88.loanonboarding.rule.RuleEvaluationService;
import com.f88.loanonboarding.rule.customer.CustomerAgeRule;
import com.f88.loanonboarding.rule.customer.CustomerBlacklistRule;
import com.f88.loanonboarding.service.CustomerService;

@Service
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final RuleEvaluationService ruleEvaluationService;
    private final CustomerAgeRule customerAgeRule;

    public CustomerServiceImpl(
            CustomerRepository customerRepository,
            RuleEvaluationService ruleEvaluationService,
            CustomerAgeRule customerAgeRule
    ) {
        this.customerRepository = customerRepository;
        this.ruleEvaluationService = ruleEvaluationService;
        this.customerAgeRule = customerAgeRule;
    }

    @Override
    public CustomerLookupResponse lookup(CustomerLookupRequest request) {
        ruleEvaluationService.validateOrThrow(
                RuleContext.customer(null, request.dateOfBirth(), false),
                List.of(new CustomerBlacklistRule(), customerAgeRule)
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
        boolean eligible = CustomerStatus.ACTIVE.equals(customer.getStatus());
        return new CustomerLookupResponse(
                true,
                customer.getCustomerCode(),
                customer.getStatus().name(),
                eligible ? "ELIGIBLE" : "NOT_ELIGIBLE",
                eligible ? "ALLOWED" : "BLOCKED",
                new MatchedCustomerResponse(
                        customer.getFullName(),
                        customer.getDateOfBirth(),
                        customer.getIdentityNumber(),
                        customer.getPhoneNumber()
                ),
                eligible ? null : "CUSTOMER_" + customer.getStatus().name()
        );
    }
}
