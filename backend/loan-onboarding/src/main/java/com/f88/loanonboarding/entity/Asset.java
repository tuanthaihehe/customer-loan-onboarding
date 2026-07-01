package com.f88.loanonboarding.entity;

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
@Table(name = "asset")
public class Asset {

    @Id
    private UUID id = UUID.randomUUID();

    @Column(name = "asset_code", nullable = false, unique = true, length = 50)
    private String assetCode;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vehicle_variant_id", nullable = false)
    private VehicleVariant vehicleVariant;

    @Column(name = "license_plate", nullable = false, unique = true, length = 20)
    private String licensePlate;

    @Column(name = "status", nullable = false, length = 30)
    private String status = "AVAILABLE";
}
