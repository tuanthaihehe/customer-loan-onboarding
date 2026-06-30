package com.f88.loanonboarding.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "loan_application_state_history")
public class LoanApplicationStateHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "loan_application_id", nullable = false)
    private LoanApplicationEntity loanApplication;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_state_id")
    private LoanApplicationStateEntity fromState;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "to_state_id", nullable = false)
    private LoanApplicationStateEntity toState;

    @Column(name = "action_code", nullable = false, length = 50)
    private String actionCode;

    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;

    @Column(name = "changed_by", length = 100)
    private String changedBy;

    @Column(name = "note")
    private String note;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public LoanApplicationEntity getLoanApplication() {
        return loanApplication;
    }

    public void setLoanApplication(LoanApplicationEntity loanApplication) {
        this.loanApplication = loanApplication;
    }

    public LoanApplicationStateEntity getFromState() {
        return fromState;
    }

    public void setFromState(LoanApplicationStateEntity fromState) {
        this.fromState = fromState;
    }

    public LoanApplicationStateEntity getToState() {
        return toState;
    }

    public void setToState(LoanApplicationStateEntity toState) {
        this.toState = toState;
    }

    public String getActionCode() {
        return actionCode;
    }

    public void setActionCode(String actionCode) {
        this.actionCode = actionCode;
    }

    public LocalDateTime getChangedAt() {
        return changedAt;
    }

    public void setChangedAt(LocalDateTime changedAt) {
        this.changedAt = changedAt;
    }

    public String getChangedBy() {
        return changedBy;
    }

    public void setChangedBy(String changedBy) {
        this.changedBy = changedBy;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
