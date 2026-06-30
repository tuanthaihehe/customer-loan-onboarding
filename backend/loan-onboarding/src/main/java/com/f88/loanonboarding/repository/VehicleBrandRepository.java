package com.f88.loanonboarding.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.f88.loanonboarding.entity.VehicleBrandEntity;

public interface VehicleBrandRepository extends JpaRepository<VehicleBrandEntity, UUID> {
    List<VehicleBrandEntity> findByActiveTrueAndVehicleTypeActiveTrueOrderBySortOrderAscNameAsc();
    List<VehicleBrandEntity> findByActiveTrueAndVehicleTypeActiveTrueAndVehicleTypeCodeOrderBySortOrderAscNameAsc(String vehicleTypeCode);
}
