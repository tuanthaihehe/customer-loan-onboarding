package com.f88.loanonboarding.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.f88.loanonboarding.entity.VehicleVersion;

public interface VehicleVersionRepository extends JpaRepository<VehicleVersion, UUID> {

    List<VehicleVersion> findByVehicleModel_CodeAndActiveTrueOrderBySortOrderAsc(String modelCode);
}
