package com.f88.loanonboarding.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.f88.loanonboarding.dto.request.customer.CustomerLookupRequest;
import com.f88.loanonboarding.dto.response.customer.CustomerLookupResponse;
import com.f88.loanonboarding.mock.DemoCustomerMockDataProvider;
import com.f88.loanonboarding.rule.RuleContext;
import com.f88.loanonboarding.rule.RuleEvaluationService;
import com.f88.loanonboarding.rule.customer.CustomerAgeRule;
import com.f88.loanonboarding.rule.customer.CustomerBlacklistRule;
import com.f88.loanonboarding.service.CustomerService;

@Service
public class CustomerServiceMockImpl implements CustomerService {

    private final RuleEvaluationService ruleEvaluationService;
    private final DemoCustomerMockDataProvider mockDataProvider;

    public CustomerServiceMockImpl(
            RuleEvaluationService ruleEvaluationService,
            DemoCustomerMockDataProvider mockDataProvider
    ) {
        this.ruleEvaluationService = ruleEvaluationService;
        this.mockDataProvider = mockDataProvider;
    }

    @Override
    public CustomerLookupResponse lookup(CustomerLookupRequest request) {
        ruleEvaluationService.validateOrThrow(
                RuleContext.customer(null, request.dateOfBirth(), false),
                List.of(new CustomerBlacklistRule(), new CustomerAgeRule())
        );

        return mockDataProvider.eligibleCustomer(request);
    }
}
