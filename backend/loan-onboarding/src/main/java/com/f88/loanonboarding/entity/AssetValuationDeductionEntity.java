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
@Table(name = "asset_valuation_deduction")
public class AssetValuationDeductionEntity {
    @Id
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "asset_valuation_id", nullable = false)
    private AssetValuationEntity assetValuation;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "deduction_type_id", nullable = false)
    private AssetDeductionTypeEntity deductionType;
    @Column(name = "deduction_amount_snapshot", nullable = false, precision = 18, scale = 2)
    private BigDecimal deductionAmountSnapshot;
    @Column(name = "note")
    private String note;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public AssetValuationEntity getAssetValuation() { return assetValuation; }
    public void setAssetValuation(AssetValuationEntity assetValuation) { this.assetValuation = assetValuation; }
    public AssetDeductionTypeEntity getDeductionType() { return deductionType; }
    public void setDeductionType(AssetDeductionTypeEntity deductionType) { this.deductionType = deductionType; }
    public BigDecimal getDeductionAmountSnapshot() { return deductionAmountSnapshot; }
    public void setDeductionAmountSnapshot(BigDecimal deductionAmountSnapshot) { this.deductionAmountSnapshot = deductionAmountSnapshot; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
