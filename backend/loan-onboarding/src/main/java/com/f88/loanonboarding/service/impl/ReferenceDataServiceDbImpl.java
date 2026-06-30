package com.f88.loanonboarding.service.impl;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.f88.loanonboarding.dto.response.common.ReferenceDataItemResponse;
import com.f88.loanonboarding.service.ReferenceDataService;

@Service
public class ReferenceDataServiceDbImpl implements ReferenceDataService {

    private final JdbcTemplate jdbcTemplate;

    public ReferenceDataServiceDbImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<ReferenceDataItemResponse> getGenders() {
        return List.of();
    }

    @Override
    public List<ReferenceDataItemResponse> getOccupations() {
        return List.of();
    }

    @Override
    public List<ReferenceDataItemResponse> getLoanPurposes() {
        return queryItems(
                """
                SELECT code, name, description
                FROM loan_purpose
                WHERE is_active = true
                ORDER BY sort_order, name
                """
        );
    }

    @Override
    public List<ReferenceDataItemResponse> getAssetTypes() {
        return queryItems(
                """
                SELECT code, name, description
                FROM vehicle_type
                WHERE is_active = true
                ORDER BY sort_order, name
                """
        );
    }

    @Override
    public List<ReferenceDataItemResponse> getVehicleBrands(String assetType) {
        return jdbcTemplate.query(
                """
                SELECT vb.code, vb.name, NULL::text AS description
                FROM vehicle_brand vb
                JOIN vehicle_type vt ON vt.id = vb.vehicle_type_id
                WHERE vb.is_active = true
                  AND vt.is_active = true
                  AND (? IS NULL OR vt.code = ?)
                ORDER BY vb.sort_order, vb.name
                """,
                (rs, rowNum) -> item(rs.getString("code"), rs.getString("name"), rs.getString("description")),
                assetType,
                assetType
        );
    }

    @Override
    public List<ReferenceDataItemResponse> getVehicleModels(String brandCode) {
        return jdbcTemplate.query(
                """
                SELECT vm.code, vm.name, NULL::text AS description
                FROM vehicle_model vm
                JOIN vehicle_brand vb ON vb.id = vm.vehicle_brand_id
                WHERE vm.is_active = true
                  AND vb.is_active = true
                  AND (? IS NULL OR vb.code = ?)
                ORDER BY vm.sort_order, vm.name
                """,
                (rs, rowNum) -> item(rs.getString("code"), rs.getString("name"), rs.getString("description")),
                brandCode,
                brandCode
        );
    }

    @Override
    public List<ReferenceDataItemResponse> getVehicleVariants(String modelCode) {
        return jdbcTemplate.query(
                """
                SELECT vv.code, vv.name, NULL::text AS description
                FROM vehicle_version vv
                JOIN vehicle_model vm ON vm.id = vv.vehicle_model_id
                WHERE vv.is_active = true
                  AND vm.is_active = true
                  AND (? IS NULL OR vm.code = ?)
                ORDER BY vv.sort_order, vv.name
                """,
                (rs, rowNum) -> item(rs.getString("code"), rs.getString("name"), rs.getString("description")),
                modelCode,
                modelCode
        );
    }

    @Override
    public List<ReferenceDataItemResponse> getManufactureYears() {
        return jdbcTemplate.query(
                """
                SELECT DISTINCT
                       manufacture_year::text AS code,
                       manufacture_year::text AS name,
                       NULL::text AS description
                FROM vehicle_year
                WHERE is_active = true
                ORDER BY manufacture_year DESC
                """,
                (rs, rowNum) -> item(rs.getString("code"), rs.getString("name"), rs.getString("description"))
        );
    }

    @Override
    public List<ReferenceDataItemResponse> getVehicleColors() {
        return queryItems(
                """
                SELECT code, name, NULL::text AS description
                FROM vehicle_color
                WHERE is_active = true
                ORDER BY sort_order, name
                """
        );
    }

    @Override
    public List<ReferenceDataItemResponse> getValuationDeductionFactors() {
        return queryItems(
                """
                SELECT code, name, description
                FROM asset_deduction_type
                WHERE is_active = true
                ORDER BY sort_order, name
                """
        );
    }

    private List<ReferenceDataItemResponse> queryItems(String sql) {
        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> item(rs.getString("code"), rs.getString("name"), rs.getString("description"))
        );
    }

    private ReferenceDataItemResponse item(String code, String name, String description) {
        return new ReferenceDataItemResponse(code, name, description);
    }
}
