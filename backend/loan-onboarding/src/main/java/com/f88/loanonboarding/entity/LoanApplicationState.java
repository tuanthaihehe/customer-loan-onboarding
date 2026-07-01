package com.f88.loanonboarding.entity;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "loan_application_state")
public class LoanApplicationState {

    @Id
    private UUID id = UUID.randomUUID();

    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "is_initial", nullable = false)
    private boolean initial;

    @Column(name = "is_terminal", nullable = false)
    private boolean terminal;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;
}
