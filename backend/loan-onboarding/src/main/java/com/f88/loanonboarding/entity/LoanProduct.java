package com.f88.loanonboarding.entity;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "loan_product")
public class LoanProduct {

    @Id
    private UUID id = UUID.randomUUID();

    @Column(name = "product_code", nullable = false, unique = true, length = 50)
    private String productCode;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(name = "applies_to_all_loan_purposes", nullable = false)
    private boolean appliesToAllLoanPurposes;

    @Column(name = "min_loan_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal minLoanAmount;

    @Column(name = "max_loan_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal maxLoanAmount;

    @Column(name = "max_ltv_percent", nullable = false, precision = 5, scale = 2)
    private BigDecimal maxLtvPercent;

    @Column(name = "monthly_interest_rate_percent", nullable = false, precision = 5, scale = 2)
    private BigDecimal monthlyInterestRatePercent;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "loan_product_purpose",
            joinColumns = @JoinColumn(name = "loan_product_id"),
            inverseJoinColumns = @JoinColumn(name = "loan_purpose_id")
    )
    private Set<LoanPurpose> loanPurposes = new LinkedHashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "loan_product_vehicle_type",
            joinColumns = @JoinColumn(name = "loan_product_id"),
            inverseJoinColumns = @JoinColumn(name = "vehicle_type_id")
    )
    private Set<VehicleType> vehicleTypes = new LinkedHashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "loan_product_term",
            joinColumns = @JoinColumn(name = "loan_product_id"),
            inverseJoinColumns = @JoinColumn(name = "loan_term_id")
    )
    private Set<LoanTerm> loanTerms = new LinkedHashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "loan_product_score_grade",
            joinColumns = @JoinColumn(name = "loan_product_id"),
            inverseJoinColumns = @JoinColumn(name = "score_grade_id")
    )
    private Set<ScoreGrade> scoreGrades = new LinkedHashSet<>();
}
