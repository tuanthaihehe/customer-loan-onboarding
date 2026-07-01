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
@Table(name = "asset_valuation")
public class AssetValuation {

    @Id
    private UUID id = UUID.randomUUID();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "asset_id", nullable = false)
    private Asset asset;

    @Column(name = "market_price_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal marketPriceAmount;

    @Column(name = "total_deduction_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal totalDeductionAmount = BigDecimal.ZERO;

    @Column(name = "final_value_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal finalValueAmount;

    @Column(name = "currency_code", nullable = false, length = 3)
    private String currencyCode = "VND";

    @Column(name = "valuation_source", length = 100)
    private String valuationSource;

    @Column(name = "valued_at", nullable = false)
    private LocalDateTime valuedAt;

    @Column(name = "valued_by", length = 100)
    private String valuedBy;

    @Column(name = "note")
    private String note;

    @PrePersist
    void prePersist() {
        if (valuedAt == null) {
            valuedAt = LocalDateTime.now();
        }
    }
}
