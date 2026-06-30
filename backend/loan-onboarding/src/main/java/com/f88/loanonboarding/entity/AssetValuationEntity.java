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
import jakarta.persistence.Table;

@Entity
@Table(name = "asset_valuation")
public class AssetValuationEntity {
    @Id
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "asset_id", nullable = false)
    private AssetEntity asset;
    @Column(name = "market_price_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal marketPriceAmount;
    @Column(name = "total_deduction_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal totalDeductionAmount;
    @Column(name = "final_value_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal finalValueAmount;
    @Column(name = "currency_code", nullable = false, length = 3)
    private String currencyCode;
    @Column(name = "valuation_source")
    private String valuationSource;
    @Column(name = "valued_at", nullable = false)
    private LocalDateTime valuedAt;
    @Column(name = "valued_by")
    private String valuedBy;
    @Column(name = "note")
    private String note;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public AssetEntity getAsset() { return asset; }
    public void setAsset(AssetEntity asset) { this.asset = asset; }
    public BigDecimal getMarketPriceAmount() { return marketPriceAmount; }
    public void setMarketPriceAmount(BigDecimal marketPriceAmount) { this.marketPriceAmount = marketPriceAmount; }
    public BigDecimal getTotalDeductionAmount() { return totalDeductionAmount; }
    public void setTotalDeductionAmount(BigDecimal totalDeductionAmount) { this.totalDeductionAmount = totalDeductionAmount; }
    public BigDecimal getFinalValueAmount() { return finalValueAmount; }
    public void setFinalValueAmount(BigDecimal finalValueAmount) { this.finalValueAmount = finalValueAmount; }
    public String getCurrencyCode() { return currencyCode; }
    public void setCurrencyCode(String currencyCode) { this.currencyCode = currencyCode; }
    public String getValuationSource() { return valuationSource; }
    public void setValuationSource(String valuationSource) { this.valuationSource = valuationSource; }
    public LocalDateTime getValuedAt() { return valuedAt; }
    public void setValuedAt(LocalDateTime valuedAt) { this.valuedAt = valuedAt; }
    public String getValuedBy() { return valuedBy; }
    public void setValuedBy(String valuedBy) { this.valuedBy = valuedBy; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
