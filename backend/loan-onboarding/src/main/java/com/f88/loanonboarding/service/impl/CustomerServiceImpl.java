package com.f88.loanonboarding.service.impl;

import java.time.Year;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.f88.loanonboarding.common.error.ErrorCode;
import com.f88.loanonboarding.dto.request.customer.CreateCustomerRequest;
import com.f88.loanonboarding.dto.request.customer.CustomerLookupRequest;
import com.f88.loanonboarding.dto.response.customer.CreatedCustomerResponse;
import com.f88.loanonboarding.dto.response.customer.CustomerLookupResponse;
import com.f88.loanonboarding.dto.response.customer.MatchedCustomerResponse;
import com.f88.loanonboarding.entity.Customer;
import com.f88.loanonboarding.exception.BusinessException;
import com.f88.loanonboarding.repository.CustomerRepository;
import com.f88.loanonboarding.rule.RuleContext;
import com.f88.loanonboarding.rule.RuleEvaluationService;
import com.f88.loanonboarding.rule.customer.CustomerAgeRule;
import com.f88.loanonboarding.rule.customer.CustomerBlacklistRule;
import com.f88.loanonboarding.service.CustomerService;

@Service
public class CustomerServiceImpl implements CustomerService {

    private static final String STATUS_ACTIVE = "ACTIVE";
    private static final String STATUS_LEAD = "LEAD";

    private final CustomerRepository customerRepository;
    private final RuleEvaluationService ruleEvaluationService;

    public CustomerServiceImpl(CustomerRepository customerRepository, RuleEvaluationService ruleEvaluationService) {
        this.customerRepository = customerRepository;
        this.ruleEvaluationService = ruleEvaluationService;
    }

    @Override
    public CustomerLookupResponse lookup(CustomerLookupRequest request) {
        List<Customer> customers = customerRepository.lookup(
                request.identifierNumber(),
                request.phoneNumber(),
                request.fullName(),
                request.dateOfBirth()
        );

        if (customers.isEmpty()) {
            ruleEvaluationService.validateOrThrow(
                    RuleContext.customer(null, request.dateOfBirth(), false),
                    List.of(new CustomerBlacklistRule(), new CustomerAgeRule())
            );
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

        return toLookupResponse(customers.get(0));
    }

    @Override
    @Transactional
    public CreatedCustomerResponse create(CreateCustomerRequest request) {
        validateCreatable(request);

        Customer customer = new Customer();
        customer.setCustomerCode(nextCustomerCode());
        customer.setFullName(normalizeText(request.fullName()));
        customer.setIdentityNumber(normalizeText(request.identifierNumber()));
        customer.setPhoneNumber(normalizeText(request.phoneNumber()));
        customer.setDateOfBirth(request.dateOfBirth());
        customer.setStatus(STATUS_LEAD);

        return toCreatedResponse(customerRepository.save(customer));
    }

    private CustomerLookupResponse toLookupResponse(Customer customer) {
        boolean eligible = STATUS_ACTIVE.equals(customer.getStatus());
        boolean restricted = "BLACKLIST".equals(customer.getStatus()) || "RESTRICTED".equals(customer.getStatus());
        boolean canCreateApplication = !restricted;
        ruleEvaluationService.validateOrThrow(
                RuleContext.customer(customer.getCustomerCode(), customer.getDateOfBirth(), restricted),
                List.of(new CustomerBlacklistRule(), new CustomerAgeRule())
        );
        return new CustomerLookupResponse(
                true,
                customer.getCustomerCode(),
                customer.getStatus(),
                eligible ? "ELIGIBLE" : customer.getStatus(),
                canCreateApplication ? "ALLOW_CREATE_APPLICATION" : "BLOCKED",
                new MatchedCustomerResponse(
                        customer.getFullName(),
                        customer.getDateOfBirth(),
                        customer.getIdentityNumber(),
                        customer.getPhoneNumber()
                ),
                canCreateApplication ? null : "CUSTOMER_" + customer.getStatus()
        );
    }

    private void validateCreatable(CreateCustomerRequest request) {
        ruleEvaluationService.validateOrThrow(
                RuleContext.customer(null, request.dateOfBirth(), false),
                List.of(new CustomerBlacklistRule(), new CustomerAgeRule())
        );

        String identityNumber = normalizeText(request.identifierNumber());
        if (customerRepository.existsByIdentityNumber(identityNumber)) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Số giấy tờ đã tồn tại trong database, vui lòng tra cứu lại khách hàng");
        }

        String phoneNumber = normalizeText(request.phoneNumber());
        if (customerRepository.existsByPhoneNumber(phoneNumber)) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Số điện thoại đã tồn tại trong database, vui lòng tra cứu lại khách hàng");
        }
    }

    private String nextCustomerCode() {
        String prefix = "CUS-" + Year.now().getValue() + "-";
        String lastCode = customerRepository
                .findTopByCustomerCodeStartingWithOrderByCustomerCodeDesc(prefix)
                .map(Customer::getCustomerCode)
                .orElse(null);
        int next = lastCode == null ? 1 : parseCustomerSequence(lastCode) + 1;
        return prefix + "%06d".formatted(next);
    }

    private int parseCustomerSequence(String customerCode) {
        int delimiter = customerCode.lastIndexOf('-');
        if (delimiter < 0 || delimiter == customerCode.length() - 1) {
            return 0;
        }
        try {
            return Integer.parseInt(customerCode.substring(delimiter + 1));
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    private String normalizeText(String value) {
        return value == null ? null : value.trim();
    }

    private CreatedCustomerResponse toCreatedResponse(Customer customer) {
        return new CreatedCustomerResponse(
                customer.getCustomerCode(),
                customer.getFullName(),
                customer.getIdentityNumber(),
                customer.getPhoneNumber(),
                customer.getDateOfBirth(),
                customer.getStatus()
        );
    }
}
