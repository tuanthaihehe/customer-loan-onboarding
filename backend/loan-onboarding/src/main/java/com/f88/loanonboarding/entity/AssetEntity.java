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
@Table(name = "asset")
public class AssetEntity {
    @Id
    private UUID id;
    @Column(name = "asset_code", nullable = false)
    private String assetCode;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vehicle_variant_id", nullable = false)
    private VehicleVariantEntity vehicleVariant;
    @Column(name = "license_plate", nullable = false)
    private String licensePlate;
    @Column(name = "status", nullable = false)
    private String status;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getAssetCode() { return assetCode; }
    public void setAssetCode(String assetCode) { this.assetCode = assetCode; }
    public VehicleVariantEntity getVehicleVariant() { return vehicleVariant; }
    public void setVehicleVariant(VehicleVariantEntity vehicleVariant) { this.vehicleVariant = vehicleVariant; }
    public String getLicensePlate() { return licensePlate; }
    public void setLicensePlate(String licensePlate) { this.licensePlate = licensePlate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
