package com.f88.loanonboarding.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.f88.loanonboarding.entity.VehicleModelEntity;

public interface VehicleModelRepository extends JpaRepository<VehicleModelEntity, UUID> {
    List<VehicleModelEntity> findByActiveTrueAndVehicleBrandActiveTrueOrderBySortOrderAscNameAsc();
    List<VehicleModelEntity> findByActiveTrueAndVehicleBrandActiveTrueAndVehicleBrandCodeOrderBySortOrderAscNameAsc(String brandCode);
}
