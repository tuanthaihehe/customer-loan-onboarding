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
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "asset_valuation_deduction")
public class AssetValuationDeduction {

    @Id
    private UUID id = UUID.randomUUID();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "asset_valuation_id", nullable = false)
    private AssetValuation assetValuation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "deduction_type_id", nullable = false)
    private AssetDeductionType deductionType;

    @Column(name = "deduction_amount_snapshot", nullable = false, precision = 18, scale = 2)
    private BigDecimal deductionAmountSnapshot;

    @Column(name = "note")
    private String note;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
