package com.f88.loanonboarding.entity;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "loan_application_state_transition")
public class LoanApplicationStateTransitionEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "from_state_id", nullable = false)
    private LoanApplicationStateEntity fromState;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "to_state_id", nullable = false)
    private LoanApplicationStateEntity toState;

    @Column(name = "action_code", nullable = false, length = 50)
    private String actionCode;

    @Column(name = "action_name", nullable = false, length = 100)
    private String actionName;

    @Column(name = "description")
    private String description;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
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

    public String getActionName() {
        return actionName;
    }

    public void setActionName(String actionName) {
        this.actionName = actionName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
