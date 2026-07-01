package com.f88.loanonboarding.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.f88.loanonboarding.entity.VehicleBrand;

public interface VehicleBrandRepository extends JpaRepository<VehicleBrand, UUID> {

    List<VehicleBrand> findByActiveTrueOrderBySortOrderAsc();

    List<VehicleBrand> findByVehicleType_CodeAndActiveTrueOrderBySortOrderAsc(String vehicleTypeCode);

    List<VehicleBrand> findByActiveTrueAndVehicleTypeActiveTrueOrderBySortOrderAscNameAsc();

    List<VehicleBrand> findByActiveTrueAndVehicleTypeActiveTrueAndVehicleTypeCodeOrderBySortOrderAscNameAsc(String vehicleTypeCode);
}
