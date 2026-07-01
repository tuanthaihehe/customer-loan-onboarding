package com.f88.loanonboarding.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.f88.loanonboarding.entity.VehicleColor;

public interface VehicleColorRepository extends JpaRepository<VehicleColor, UUID> {

    List<VehicleColor> findByActiveTrueOrderBySortOrderAsc();

    @Query("""
            select distinct color
            from VehicleVariant variant
            join variant.vehicleColor color
            join variant.vehicleYear vehicleYear
            join vehicleYear.vehicleVersion vehicleVersion
            join vehicleVersion.vehicleModel vehicleModel
            where vehicleModel.code = :modelCode
              and vehicleVersion.code = :versionCode
              and vehicleYear.manufactureYear = :manufactureYear
              and variant.active = true
              and color.active = true
            order by color.sortOrder asc
            """)
    List<VehicleColor> findActiveByModelCodeAndVersionCodeAndManufactureYear(
            @Param("modelCode") String modelCode,
            @Param("versionCode") String versionCode,
            @Param("manufactureYear") Integer manufactureYear
    );
}
