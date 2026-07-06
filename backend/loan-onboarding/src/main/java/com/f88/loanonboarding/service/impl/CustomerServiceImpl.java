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
import com.f88.loanonboarding.enums.CustomerStatus;
import com.f88.loanonboarding.exception.BusinessException;
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
        String identityNumber = normalizeText(request.identifierNumber());
        String phoneNumber = normalizeText(request.phoneNumber());
        String fullName = normalizeText(request.fullName());

        List<Customer> exactMatches = customerRepository.lookupExact(
                identityNumber,
                phoneNumber,
                fullName,
                request.dateOfBirth()
        );
        if (!exactMatches.isEmpty()) {
            return toLookupResponse(exactMatches.get(0));
        }

        List<Customer> conflictCandidates = customerRepository.findIdentityConflictCandidates(identityNumber, phoneNumber);
        if (!conflictCandidates.isEmpty()) {
            return identityMismatchResponse(conflictCandidates.get(0), identityNumber, phoneNumber);
        }

        ruleEvaluationService.validateOrThrow(
                RuleContext.customer(null, request.dateOfBirth(), false),
                List.of(new CustomerBlacklistRule(), customerAgeRule)
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
        customer.setStatus(CustomerStatus.LEAD);

        return toCreatedResponse(customerRepository.save(customer));
    }

    private CustomerLookupResponse toLookupResponse(Customer customer) {
        CustomerStatus status = customer.getStatus();
        boolean eligible = CustomerStatus.ACTIVE.equals(status);
        boolean restricted = CustomerStatus.BLACKLIST.equals(status);
        boolean canCreateApplication = !restricted;

        if (restricted) {
            return new CustomerLookupResponse(
                    true,
                    customer.getCustomerCode(),
                    status.name(),
                    status.name(),
                    "BLOCKED",
                    matchedCustomer(customer),
                    "CUSTOMER_" + status.name()
            );
        }

        ruleEvaluationService.validateOrThrow(
                RuleContext.customer(customer.getCustomerCode(), customer.getDateOfBirth(), restricted),
                List.of(new CustomerBlacklistRule(), customerAgeRule)
        );
        return new CustomerLookupResponse(
                true,
                customer.getCustomerCode(),
                status.name(),
                eligible ? "ELIGIBLE" : status.name(),
                canCreateApplication ? "ALLOW_CREATE_APPLICATION" : "BLOCKED",
                matchedCustomer(customer),
                canCreateApplication ? null : "CUSTOMER_" + status.name()
        );
    }

    private CustomerLookupResponse identityMismatchResponse(Customer customer, String identityNumber, String phoneNumber) {
        return new CustomerLookupResponse(
                false,
                null,
                null,
                "IDENTITY_MISMATCH",
                "BLOCKED",
                null,
                mismatchReasonCode(customer, identityNumber, phoneNumber)
        );
    }

    private String mismatchReasonCode(Customer customer, String identityNumber, String phoneNumber) {
        boolean identityMatched = identityNumber != null && identityNumber.equals(customer.getIdentityNumber());
        boolean phoneMatched = phoneNumber != null && phoneNumber.equals(customer.getPhoneNumber());
        if (identityMatched && phoneMatched) {
            return "CUSTOMER_IDENTITY_INFO_MISMATCH";
        }
        if (identityMatched) {
            return "CUSTOMER_IDENTITY_NUMBER_MISMATCH";
        }
        return "CUSTOMER_PHONE_NUMBER_MISMATCH";
    }

    private MatchedCustomerResponse matchedCustomer(Customer customer) {
        return new MatchedCustomerResponse(
                customer.getFullName(),
                customer.getDateOfBirth(),
                customer.getIdentityNumber(),
                customer.getPhoneNumber()
        );
    }

    private void validateCreatable(CreateCustomerRequest request) {
        ruleEvaluationService.validateOrThrow(
                RuleContext.customer(null, request.dateOfBirth(), false),
                List.of(new CustomerBlacklistRule(), customerAgeRule)
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
                customer.getStatus().name()
        );
    }
}
