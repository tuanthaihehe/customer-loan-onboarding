package com.f88.loanonboarding.entity;

import java.math.BigDecimal;
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
@Table(name = "mock_score_grade_rule")
public class MockScoreGradeRule {

    @Id
    private UUID id = UUID.randomUUID();

    @Column(name = "rule_code", nullable = false, unique = true, length = 50)
    private String ruleCode;

    @Column(name = "rule_name", nullable = false)
    private String ruleName;

    @Column(name = "description")
    private String description;

    @Column(name = "min_monthly_income_amount", precision = 18, scale = 2)
    private BigDecimal minMonthlyIncomeAmount;

    @Column(name = "max_monthly_income_amount", precision = 18, scale = 2)
    private BigDecimal maxMonthlyIncomeAmount;

    @Column(name = "min_requested_amount", precision = 18, scale = 2)
    private BigDecimal minRequestedAmount;

    @Column(name = "max_requested_amount", precision = 18, scale = 2)
    private BigDecimal maxRequestedAmount;

    @Column(name = "min_ltv_percent", precision = 5, scale = 2)
    private BigDecimal minLtvPercent;

    @Column(name = "max_ltv_percent", precision = 5, scale = 2)
    private BigDecimal maxLtvPercent;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "score_grade_id", nullable = false)
    private ScoreGrade scoreGrade;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;
}
