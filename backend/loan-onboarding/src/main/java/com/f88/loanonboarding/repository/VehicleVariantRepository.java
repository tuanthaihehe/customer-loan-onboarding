package com.f88.loanonboarding.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.f88.loanonboarding.entity.VehicleVariant;

public interface VehicleVariantRepository extends JpaRepository<VehicleVariant, UUID> {

    @EntityGraph(attributePaths = {
            "vehicleColor",
            "vehicleYear",
            "vehicleYear.vehicleVersion",
            "vehicleYear.vehicleVersion.vehicleModel",
            "vehicleYear.vehicleVersion.vehicleModel.vehicleBrand",
            "vehicleYear.vehicleVersion.vehicleModel.vehicleBrand.vehicleType"
    })
    Optional<VehicleVariant> findByCode(String code);

    @Query("""
            select variant
            from VehicleVariant variant
            join variant.vehicleYear vehicleYear
            join vehicleYear.vehicleVersion vehicleVersion
            join vehicleVersion.vehicleModel vehicleModel
            where vehicleModel.code = :modelCode
              and variant.active = true
            order by variant.sortOrder asc
            """)
    List<VehicleVariant> findActiveByModelCode(@Param("modelCode") String modelCode);

    @EntityGraph(attributePaths = {
            "vehicleColor",
            "vehicleYear",
            "vehicleYear.vehicleVersion",
            "vehicleYear.vehicleVersion.vehicleModel",
            "vehicleYear.vehicleVersion.vehicleModel.vehicleBrand",
            "vehicleYear.vehicleVersion.vehicleModel.vehicleBrand.vehicleType"
    })
    @Query("""
            select variant
            from VehicleVariant variant
            join variant.vehicleColor color
            join variant.vehicleYear vehicleYear
            join vehicleYear.vehicleVersion vehicleVersion
            where vehicleVersion.code = :versionCode
              and vehicleYear.manufactureYear = :manufactureYear
              and color.code = :colorCode
              and variant.active = true
            """)
    Optional<VehicleVariant> findActiveByVersionCodeAndManufactureYearAndColorCode(
            @Param("versionCode") String versionCode,
            @Param("manufactureYear") Integer manufactureYear,
            @Param("colorCode") String colorCode
    );
}
