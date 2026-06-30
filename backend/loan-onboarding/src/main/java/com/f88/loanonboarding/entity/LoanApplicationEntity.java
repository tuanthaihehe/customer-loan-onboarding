package com.f88.loanonboarding.entity;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "loan_application")
public class LoanApplicationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "loan_application_code", nullable = false, unique = true, length = 50)
    private String loanApplicationCode;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private CustomerEntity customer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "current_state_id", nullable = false)
    private LoanApplicationStateEntity currentState;

    @Column(name = "requested_amount", precision = 18, scale = 2)
    private BigDecimal requestedAmount;

    @Column(name = "loan_term_months")
    private Integer loanTermMonths;

    @Column(name = "branch")
    private String branch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_purpose_id")
    private LoanPurposeEntity loanPurpose;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_term_id")
    private LoanTermEntity loanTerm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id")
    private AssetEntity asset;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getLoanApplicationCode() {
        return loanApplicationCode;
    }

    public void setLoanApplicationCode(String loanApplicationCode) {
        this.loanApplicationCode = loanApplicationCode;
    }

    public CustomerEntity getCustomer() {
        return customer;
    }

    public void setCustomer(CustomerEntity customer) {
        this.customer = customer;
    }

    public LoanApplicationStateEntity getCurrentState() {
        return currentState;
    }

    public void setCurrentState(LoanApplicationStateEntity currentState) {
        this.currentState = currentState;
    }

    public BigDecimal getRequestedAmount() {
        return requestedAmount;
    }

    public void setRequestedAmount(BigDecimal requestedAmount) {
        this.requestedAmount = requestedAmount;
    }

    public Integer getLoanTermMonths() {
        return loanTermMonths;
    }

    public void setLoanTermMonths(Integer loanTermMonths) {
        this.loanTermMonths = loanTermMonths;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public LoanPurposeEntity getLoanPurpose() {
        return loanPurpose;
    }

    public void setLoanPurpose(LoanPurposeEntity loanPurpose) {
        this.loanPurpose = loanPurpose;
    }

    public LoanTermEntity getLoanTerm() {
        return loanTerm;
    }

    public void setLoanTerm(LoanTermEntity loanTerm) {
        this.loanTerm = loanTerm;
    }

    public AssetEntity getAsset() {
        return asset;
    }

    public void setAsset(AssetEntity asset) {
        this.asset = asset;
    }
}
