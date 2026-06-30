package com.f88.loanonboarding.service.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.f88.loanonboarding.common.error.ErrorCode;
import com.f88.loanonboarding.dto.request.customer.CustomerLookupRequest;
import com.f88.loanonboarding.dto.response.customer.CustomerLookupResponse;
import com.f88.loanonboarding.dto.response.customer.MatchedCustomerResponse;
import com.f88.loanonboarding.exception.BusinessException;
import com.f88.loanonboarding.rule.RuleContext;
import com.f88.loanonboarding.rule.RuleEvaluationService;
import com.f88.loanonboarding.rule.customer.CustomerAgeRule;
import com.f88.loanonboarding.rule.customer.CustomerBlacklistRule;
import com.f88.loanonboarding.service.CustomerService;

@Service
public class CustomerServiceDbImpl implements CustomerService {

    private final JdbcTemplate jdbcTemplate;
    private final RuleEvaluationService ruleEvaluationService;

    public CustomerServiceDbImpl(JdbcTemplate jdbcTemplate, RuleEvaluationService ruleEvaluationService) {
        this.jdbcTemplate = jdbcTemplate;
        this.ruleEvaluationService = ruleEvaluationService;
    }

    @Override
    public CustomerLookupResponse lookup(CustomerLookupRequest request) {
        List<CustomerRow> customers = jdbcTemplate.query(
                """
                SELECT customer_code, full_name, phone_number, identity_number, date_of_birth, status
                FROM customer
                WHERE identity_number = ?
                   OR phone_number = ?
                   OR (lower(full_name) = lower(?) AND date_of_birth = ?)
                ORDER BY
                    CASE
                        WHEN identity_number = ? THEN 1
                        WHEN phone_number = ? THEN 2
                        ELSE 3
                    END
                LIMIT 1
                """,
                this::mapCustomer,
                request.identifierNumber(),
                request.phoneNumber(),
                request.fullName(),
                request.dateOfBirth(),
                request.identifierNumber(),
                request.phoneNumber()
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

        CustomerRow customer = customers.get(0);
        boolean restricted = "BLACKLIST".equals(customer.status()) || "RESTRICTED".equals(customer.status());
        ruleEvaluationService.validateOrThrow(
                RuleContext.customer(customer.customerCode(), customer.dateOfBirth(), restricted),
                List.of(new CustomerBlacklistRule(), new CustomerAgeRule())
        );

        if ("INACTIVE".equals(customer.status())) {
            throw new BusinessException(ErrorCode.CUSTOMER_RESTRICTED, "Khách hàng đang ở trạng thái không hoạt động");
        }

        return new CustomerLookupResponse(
                true,
                customer.customerCode(),
                customer.status(),
                restricted ? "BLACKLIST" : "ELIGIBLE",
                restricted ? "BLOCKED" : "ALLOW_CREATE_APPLICATION",
                new MatchedCustomerResponse(
                        customer.fullName(),
                        customer.dateOfBirth(),
                        customer.identityNumber(),
                        customer.phoneNumber()
                ),
                null
        );
    }

    private CustomerRow mapCustomer(ResultSet rs, int rowNum) throws SQLException {
        return new CustomerRow(
                rs.getString("customer_code"),
                rs.getString("full_name"),
                rs.getString("phone_number"),
                rs.getString("identity_number"),
                rs.getDate("date_of_birth") == null ? null : rs.getDate("date_of_birth").toLocalDate(),
                rs.getString("status")
        );
    }

    private record CustomerRow(
            String customerCode,
            String fullName,
            String phoneNumber,
            String identityNumber,
            java.time.LocalDate dateOfBirth,
            String status
    ) {
    }
}
