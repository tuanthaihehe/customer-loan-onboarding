package com.f88.loanonboarding.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.f88.loanonboarding.common.error.ErrorCode;
import com.f88.loanonboarding.dto.request.customer.CustomerLookupRequest;
import com.f88.loanonboarding.dto.response.customer.CustomerLookupResponse;
import com.f88.loanonboarding.dto.response.customer.MatchedCustomerResponse;
import com.f88.loanonboarding.entity.CustomerEntity;
import com.f88.loanonboarding.exception.BusinessException;
import com.f88.loanonboarding.repository.CustomerRepository;
import com.f88.loanonboarding.rule.RuleContext;
import com.f88.loanonboarding.rule.RuleEvaluationService;
import com.f88.loanonboarding.rule.customer.CustomerAgeRule;
import com.f88.loanonboarding.rule.customer.CustomerBlacklistRule;
import com.f88.loanonboarding.service.CustomerService;

@Service
public class CustomerServiceDbImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final RuleEvaluationService ruleEvaluationService;

    public CustomerServiceDbImpl(CustomerRepository customerRepository, RuleEvaluationService ruleEvaluationService) {
        this.customerRepository = customerRepository;
        this.ruleEvaluationService = ruleEvaluationService;
    }

    @Override
    public CustomerLookupResponse lookup(CustomerLookupRequest request) {
        List<CustomerEntity> customers = customerRepository.lookup(
                request.identifierNumber(),
                request.phoneNumber(),
                request.fullName(),
                request.dateOfBirth()
        );

        if (customers.isEmpty()) {
            return new CustomerLookupResponse(
                    false,
                    null,
                    null,
                    "NOT_FOUND",
                    "NEED_CREATE_CUSTOMER",
                    null,
                    "CUSTOMER_NOT_FOUND"
            );
        }

        CustomerEntity customer = customers.get(0);
        boolean restricted = "BLACKLIST".equals(customer.getStatus()) || "RESTRICTED".equals(customer.getStatus());
        ruleEvaluationService.validateOrThrow(
                RuleContext.customer(customer.getCustomerCode(), customer.getDateOfBirth(), restricted),
                List.of(new CustomerBlacklistRule(), new CustomerAgeRule())
        );

        if ("INACTIVE".equals(customer.getStatus())) {
            throw new BusinessException(ErrorCode.CUSTOMER_RESTRICTED, "Khách hàng đang ở trạng thái không hoạt động");
        }

        return new CustomerLookupResponse(
                true,
                customer.getCustomerCode(),
                customer.getStatus(),
                restricted ? "BLACKLIST" : "ELIGIBLE",
                restricted ? "BLOCKED" : "ALLOW_CREATE_APPLICATION",
                new MatchedCustomerResponse(
                        customer.getFullName(),
                        customer.getDateOfBirth(),
                        customer.getIdentityNumber(),
                        customer.getPhoneNumber()
                ),
                null
        );
    }
}
