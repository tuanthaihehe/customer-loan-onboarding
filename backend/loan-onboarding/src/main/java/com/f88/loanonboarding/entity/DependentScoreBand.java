package com.f88.loanonboarding.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
@Table(name = "dependent_score_band")
public class DependentScoreBand {

    @Id
    private UUID id = UUID.randomUUID();

    @Column(name = "rule_set_code", nullable = false, length = 50)
    private String ruleSetCode = "BASIC_SCORING_V1";

    @Column(name = "min_dependent_count", nullable = false)
    private int minDependentCount;

    @Column(name = "max_dependent_count")
    private Integer maxDependentCount;

    @Column(name = "score_value", nullable = false, precision = 10, scale = 2)
    private BigDecimal scoreValue;

    @Column(name = "weight", nullable = false, precision = 5, scale = 4)
    private BigDecimal weight;

    @Column(name = "display_label")
    private String displayLabel;

    @Column(name = "priority", nullable = false)
    private int priority;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private LocalDate effectiveTo;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
