package com.f88.loanonboarding.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.f88.loanonboarding.entity.VehicleYear;

public interface VehicleYearRepository extends JpaRepository<VehicleYear, UUID> {

    @Query("""
            select distinct vehicleYear.manufactureYear
            from VehicleYear vehicleYear
            where vehicleYear.active = true
            order by vehicleYear.manufactureYear desc
            """)
    List<Integer> findActiveManufactureYears();

    @Query("""
            select distinct vehicleYear.manufactureYear
            from VehicleYear vehicleYear
            join vehicleYear.vehicleVersion vehicleVersion
            join vehicleVersion.vehicleModel vehicleModel
            where vehicleModel.code = :modelCode
              and vehicleVersion.code = :versionCode
              and vehicleYear.active = true
            order by vehicleYear.manufactureYear desc
            """)
    List<Integer> findActiveManufactureYearsByModelCodeAndVersionCode(
            @Param("modelCode") String modelCode,
            @Param("versionCode") String versionCode
    );
}
