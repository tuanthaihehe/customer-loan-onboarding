-- Customer Loan Onboarding - Vehicle Catalog and Asset Schema
-- PostgreSQL dialect
--
-- Scope:
-- - Add vehicle reference/master data for dropdown selection:
--   vehicle_type -> vehicle_brand -> vehicle_model -> vehicle_version -> vehicle_year -> vehicle_variant
-- - Add vehicle_color as a reusable color catalog.
-- - Add vehicle_market_price to store market prices by vehicle_variant.
-- - Add asset to store a customer's selected vehicle asset.
--
-- Design notes:
-- - vehicle_type is used instead of asset_type because this version only models vehicle assets.
-- - vehicle_variant represents a priceable vehicle combination:
--   vehicle_year + vehicle_color.
-- - vehicle_market_price is attached to vehicle_variant, so color can affect market price.
-- - asset stores only the customer's actual asset reference:
--   asset_code, customer_id, vehicle_variant_id, license_plate, status.
-- - Detailed vehicle information is not duplicated in asset; it is resolved through vehicle_variant.
--
-- Asset status:
-- - AVAILABLE: asset is available and not currently pledged.
-- - PLEDGED: asset is currently pledged/collateralized in an active loan process.
-- - RELEASED: asset has been released from pledge.
-- - SETTLED: asset was involved in a loan that has been settled/paid off.

-- =========================================================
-- 1. Vehicle type
-- =========================================================

CREATE TABLE vehicle_type (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    code VARCHAR(50) NOT NULL,
    name VARCHAR(100) NOT NULL,
    description TEXT,

    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    sort_order INT NOT NULL DEFAULT 0,

    CONSTRAINT uq_vehicle_type_code UNIQUE (code)
);

CREATE INDEX idx_vehicle_type_active
ON vehicle_type(is_active);

-- =========================================================
-- 2. Vehicle brand
-- =========================================================

CREATE TABLE vehicle_brand (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    vehicle_type_id UUID NOT NULL REFERENCES vehicle_type(id),

    code VARCHAR(50) NOT NULL,
    name VARCHAR(100) NOT NULL,

    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    sort_order INT NOT NULL DEFAULT 0,

    CONSTRAINT uq_vehicle_brand_type_code
        UNIQUE (vehicle_type_id, code)
);

CREATE INDEX idx_vehicle_brand_vehicle_type_id
ON vehicle_brand(vehicle_type_id);

CREATE INDEX idx_vehicle_brand_active
ON vehicle_brand(is_active);

-- =========================================================
-- 3. Vehicle model
-- =========================================================

CREATE TABLE vehicle_model (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    vehicle_brand_id UUID NOT NULL REFERENCES vehicle_brand(id),

    code VARCHAR(50) NOT NULL,
    name VARCHAR(100) NOT NULL,

    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    sort_order INT NOT NULL DEFAULT 0,

    CONSTRAINT uq_vehicle_model_brand_code
        UNIQUE (vehicle_brand_id, code)
);

CREATE INDEX idx_vehicle_model_vehicle_brand_id
ON vehicle_model(vehicle_brand_id);

CREATE INDEX idx_vehicle_model_active
ON vehicle_model(is_active);

-- =========================================================
-- 4. Vehicle version
-- =========================================================

CREATE TABLE vehicle_version (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    vehicle_model_id UUID NOT NULL REFERENCES vehicle_model(id),

    code VARCHAR(50) NOT NULL,
    name VARCHAR(100) NOT NULL,

    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    sort_order INT NOT NULL DEFAULT 0,

    CONSTRAINT uq_vehicle_version_model_code
        UNIQUE (vehicle_model_id, code)
);

CREATE INDEX idx_vehicle_version_vehicle_model_id
ON vehicle_version(vehicle_model_id);

CREATE INDEX idx_vehicle_version_active
ON vehicle_version(is_active);

-- =========================================================
-- 5. Vehicle year
-- =========================================================

CREATE TABLE vehicle_year (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    vehicle_version_id UUID NOT NULL REFERENCES vehicle_version(id),

    manufacture_year INT NOT NULL,

    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    sort_order INT NOT NULL DEFAULT 0,

    CONSTRAINT uq_vehicle_year_version_year
        UNIQUE (vehicle_version_id, manufacture_year),

    CONSTRAINT chk_vehicle_year_range
        CHECK (manufacture_year BETWEEN 1980 AND 2100)
);

CREATE INDEX idx_vehicle_year_vehicle_version_id
ON vehicle_year(vehicle_version_id);

CREATE INDEX idx_vehicle_year_manufacture_year
ON vehicle_year(manufacture_year);

CREATE INDEX idx_vehicle_year_active
ON vehicle_year(is_active);

-- =========================================================
-- 6. Vehicle color
-- =========================================================

CREATE TABLE vehicle_color (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    code VARCHAR(50) NOT NULL,
    name VARCHAR(100) NOT NULL,

    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    sort_order INT NOT NULL DEFAULT 0,

    CONSTRAINT uq_vehicle_color_code UNIQUE (code)
);

CREATE INDEX idx_vehicle_color_active
ON vehicle_color(is_active);

-- =========================================================
-- 7. Vehicle variant
-- =========================================================
-- A vehicle_variant is the exact selectable/pricable combination:
-- vehicle_year + vehicle_color.
--
-- Example:
-- - Yamaha Exciter 155 ABS 2023 Blue
-- - Yamaha Exciter 155 ABS 2023 Black

CREATE TABLE vehicle_variant (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    vehicle_year_id UUID NOT NULL REFERENCES vehicle_year(id),
    vehicle_color_id UUID NOT NULL REFERENCES vehicle_color(id),

    code VARCHAR(100) NOT NULL,
    name VARCHAR(255) NOT NULL,

    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    sort_order INT NOT NULL DEFAULT 0,

    CONSTRAINT uq_vehicle_variant_year_color
        UNIQUE (vehicle_year_id, vehicle_color_id),

    CONSTRAINT uq_vehicle_variant_code
        UNIQUE (code)
);

CREATE INDEX idx_vehicle_variant_vehicle_year_id
ON vehicle_variant(vehicle_year_id);

CREATE INDEX idx_vehicle_variant_vehicle_color_id
ON vehicle_variant(vehicle_color_id);

CREATE INDEX idx_vehicle_variant_active
ON vehicle_variant(is_active);

-- =========================================================
-- 8. Vehicle market price
-- =========================================================
-- Market price is attached to vehicle_variant, not asset.
-- This allows market price to vary by vehicle color and effective date.
--
-- A variant may have multiple historical prices.
-- The current price is the record where:
-- effective_from <= CURRENT_DATE
-- and (effective_to IS NULL OR effective_to >= CURRENT_DATE)

CREATE TABLE vehicle_market_price (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    vehicle_variant_id UUID NOT NULL REFERENCES vehicle_variant(id),

    price_amount NUMERIC(18, 2) NOT NULL,
    currency_code VARCHAR(3) NOT NULL DEFAULT 'VND',

    price_source VARCHAR(100),
    effective_from DATE NOT NULL,
    effective_to DATE,

    note TEXT,

    CONSTRAINT chk_vehicle_market_price_amount
        CHECK (price_amount > 0),

    CONSTRAINT chk_vehicle_market_price_date_range
        CHECK (effective_to IS NULL OR effective_to >= effective_from)
);

CREATE INDEX idx_vehicle_market_price_variant_id
ON vehicle_market_price(vehicle_variant_id);

CREATE INDEX idx_vehicle_market_price_effective_date
ON vehicle_market_price(effective_from, effective_to);

-- Optional guard: only one current/open-ended price per variant.
CREATE UNIQUE INDEX uq_vehicle_market_price_current
ON vehicle_market_price(vehicle_variant_id)
WHERE effective_to IS NULL;

-- =========================================================
-- 9. Asset
-- =========================================================
-- asset stores the customer's actual selected vehicle.
-- It does not duplicate vehicle type/brand/model/version/year/color.
-- These values are resolved through asset.vehicle_variant_id.

CREATE TABLE asset (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    asset_code VARCHAR(50) NOT NULL,
    customer_id UUID NOT NULL REFERENCES customer(id),

    vehicle_variant_id UUID NOT NULL REFERENCES vehicle_variant(id),
    license_plate VARCHAR(20) NOT NULL,

    status VARCHAR(30) NOT NULL DEFAULT 'AVAILABLE',

    CONSTRAINT uq_asset_code UNIQUE (asset_code),
    CONSTRAINT uq_asset_license_plate UNIQUE (license_plate),

    CONSTRAINT chk_asset_status
        CHECK (status IN ('AVAILABLE', 'PLEDGED', 'RELEASED', 'SETTLED'))
);

CREATE INDEX idx_asset_customer_id
ON asset(customer_id);

CREATE INDEX idx_asset_vehicle_variant_id
ON asset(vehicle_variant_id);

CREATE INDEX idx_asset_status
ON asset(status);
