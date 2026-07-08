package com.f88.loanonboarding.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "loan_application")
public class LoanApplication {

    @Id
    private UUID id = UUID.randomUUID();

    @Column(name = "loan_application_code", nullable = false, unique = true, length = 50)
    private String loanApplicationCode;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "current_state_id", nullable = false)
    private LoanApplicationState currentState;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_step_code")
    private LoanApplicationStep currentStep;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id")
    private Asset asset;

    @Column(name = "requested_amount", precision = 18, scale = 2)
    private BigDecimal requestedAmount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_purpose_id")
    private LoanPurpose loanPurpose;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_term_id")
    private LoanTerm loanTerm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_product_id")
    private LoanProduct loanProduct;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "occupation_id")
    private Occupation occupation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "income_source_id")
    private IncomeSource incomeSource;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "disbursement_bank_id")
    private Bank disbursementBank;

    @Column(name = "loan_term_months")
    private Integer loanTermMonths;

    @Column(name = "branch")
    private String branch;

    @Column(name = "disbursement_account_number", length = 50)
    private String disbursementAccountNumber;

    @Column(name = "disbursement_account_name")
    private String disbursementAccountName;

    @Column(name = "current_address")
    private String currentAddress;

    @Column(name = "workplace_name")
    private String workplaceName;

    @Column(name = "workplace_address")
    private String workplaceAddress;

    @Column(name = "monthly_income_amount", precision = 18, scale = 2)
    private BigDecimal monthlyIncomeAmount;

    @Column(name = "expired_at")
    private LocalDateTime expiredAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
