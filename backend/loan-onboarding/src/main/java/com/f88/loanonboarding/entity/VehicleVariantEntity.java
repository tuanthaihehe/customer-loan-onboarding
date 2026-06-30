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
@Table(name = "vehicle_variant")
public class VehicleVariantEntity {
    @Id
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vehicle_year_id", nullable = false)
    private VehicleYearEntity vehicleYear;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vehicle_color_id", nullable = false)
    private VehicleColorEntity vehicleColor;
    @Column(name = "code", nullable = false)
    private String code;
    @Column(name = "name", nullable = false)
    private String name;
    @Column(name = "is_active", nullable = false)
    private boolean active;
    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public VehicleYearEntity getVehicleYear() { return vehicleYear; }
    public void setVehicleYear(VehicleYearEntity vehicleYear) { this.vehicleYear = vehicleYear; }
    public VehicleColorEntity getVehicleColor() { return vehicleColor; }
    public void setVehicleColor(VehicleColorEntity vehicleColor) { this.vehicleColor = vehicleColor; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public int getSortOrder() { return sortOrder; }
    public void setSortOrder(int sortOrder) { this.sortOrder = sortOrder; }
}
