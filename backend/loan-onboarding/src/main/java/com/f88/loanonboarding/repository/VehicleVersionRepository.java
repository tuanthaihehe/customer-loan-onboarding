package com.f88.loanonboarding.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.f88.loanonboarding.entity.VehicleVersionEntity;

public interface VehicleVersionRepository extends JpaRepository<VehicleVersionEntity, UUID> {
    List<VehicleVersionEntity> findByActiveTrueAndVehicleModelActiveTrueOrderBySortOrderAscNameAsc();
    List<VehicleVersionEntity> findByActiveTrueAndVehicleModelActiveTrueAndVehicleModelCodeOrderBySortOrderAscNameAsc(String modelCode);
}
