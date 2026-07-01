package com.f88.loanonboarding.entity;

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
@Table(name = "loan_application_state_transition")
public class LoanApplicationStateTransition {

    @Id
    private UUID id = UUID.randomUUID();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "from_state_id", nullable = false)
    private LoanApplicationState fromState;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "to_state_id", nullable = false)
    private LoanApplicationState toState;

    @Column(name = "action_code", nullable = false, length = 50)
    private String actionCode;

    @Column(name = "action_name", nullable = false, length = 100)
    private String actionName;

    @Column(name = "description")
    private String description;
}
