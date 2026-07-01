package com.f88.loanonboarding.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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

    @Column(name = "loan_term_months")
    private Integer loanTermMonths;

    @Column(name = "branch")
    private String branch;

    @Column(name = "applicant_full_name")
    private String applicantFullName;

    @Column(name = "applicant_identity_number", length = 20)
    private String applicantIdentityNumber;

    @Column(name = "applicant_phone_number", length = 20)
    private String applicantPhoneNumber;

    @Column(name = "applicant_date_of_birth")
    private LocalDate applicantDateOfBirth;

    @Column(name = "applicant_gender", length = 30)
    private String applicantGender;

    @Column(name = "applicant_occupation", length = 100)
    private String applicantOccupation;

    @Column(name = "applicant_monthly_income", precision = 18, scale = 2)
    private BigDecimal applicantMonthlyIncome;
}
