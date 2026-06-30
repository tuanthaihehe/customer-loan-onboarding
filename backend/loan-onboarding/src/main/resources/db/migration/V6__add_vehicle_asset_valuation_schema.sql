-- Customer Loan Onboarding - Vehicle Asset and Valuation Schema
-- PostgreSQL dialect
--
-- This migration folds the latest database design from database/migrations and
-- database/seed into the backend Flyway path. It is intentionally written as a
-- single V6 because the backend Flyway history already uses V1..V5.

-- =========================================================
-- 1. Customer status update
-- =========================================================

ALTER TABLE customer
DROP CONSTRAINT IF EXISTS chk_customer_status;

UPDATE customer
SET status = 'BLACKLIST'
WHERE status = 'RESTRICTED';

ALTER TABLE customer
ADD CONSTRAINT chk_customer_status
CHECK (status IN ('ACTIVE', 'INACTIVE', 'BLACKLIST'));

-- =========================================================
-- 2. Vehicle catalog
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

CREATE TABLE vehicle_brand (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    vehicle_type_id UUID NOT NULL REFERENCES vehicle_type(id),
    code VARCHAR(50) NOT NULL,
    name VARCHAR(100) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    sort_order INT NOT NULL DEFAULT 0,
    CONSTRAINT uq_vehicle_brand_type_code UNIQUE (vehicle_type_id, code)
);

CREATE INDEX idx_vehicle_brand_vehicle_type_id
ON vehicle_brand(vehicle_type_id);

CREATE INDEX idx_vehicle_brand_active
ON vehicle_brand(is_active);

CREATE TABLE vehicle_model (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    vehicle_brand_id UUID NOT NULL REFERENCES vehicle_brand(id),
    code VARCHAR(50) NOT NULL,
    name VARCHAR(100) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    sort_order INT NOT NULL DEFAULT 0,
    CONSTRAINT uq_vehicle_model_brand_code UNIQUE (vehicle_brand_id, code)
);

CREATE INDEX idx_vehicle_model_vehicle_brand_id
ON vehicle_model(vehicle_brand_id);

CREATE INDEX idx_vehicle_model_active
ON vehicle_model(is_active);

CREATE TABLE vehicle_version (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    vehicle_model_id UUID NOT NULL REFERENCES vehicle_model(id),
    code VARCHAR(50) NOT NULL,
    name VARCHAR(100) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    sort_order INT NOT NULL DEFAULT 0,
    CONSTRAINT uq_vehicle_version_model_code UNIQUE (vehicle_model_id, code)
);

CREATE INDEX idx_vehicle_version_vehicle_model_id
ON vehicle_version(vehicle_model_id);

CREATE INDEX idx_vehicle_version_active
ON vehicle_version(is_active);

CREATE TABLE vehicle_year (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    vehicle_version_id UUID NOT NULL REFERENCES vehicle_version(id),
    manufacture_year INT NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    sort_order INT NOT NULL DEFAULT 0,
    CONSTRAINT uq_vehicle_year_version_year UNIQUE (vehicle_version_id, manufacture_year),
    CONSTRAINT chk_vehicle_year_range CHECK (manufacture_year BETWEEN 1980 AND 2100)
);

CREATE INDEX idx_vehicle_year_vehicle_version_id
ON vehicle_year(vehicle_version_id);

CREATE INDEX idx_vehicle_year_manufacture_year
ON vehicle_year(manufacture_year);

CREATE INDEX idx_vehicle_year_active
ON vehicle_year(is_active);

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

CREATE TABLE vehicle_variant (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    vehicle_year_id UUID NOT NULL REFERENCES vehicle_year(id),
    vehicle_color_id UUID NOT NULL REFERENCES vehicle_color(id),
    code VARCHAR(100) NOT NULL,
    name VARCHAR(255) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    sort_order INT NOT NULL DEFAULT 0,
    CONSTRAINT uq_vehicle_variant_year_color UNIQUE (vehicle_year_id, vehicle_color_id),
    CONSTRAINT uq_vehicle_variant_code UNIQUE (code)
);

CREATE INDEX idx_vehicle_variant_vehicle_year_id
ON vehicle_variant(vehicle_year_id);

CREATE INDEX idx_vehicle_variant_vehicle_color_id
ON vehicle_variant(vehicle_color_id);

CREATE INDEX idx_vehicle_variant_active
ON vehicle_variant(is_active);

CREATE TABLE vehicle_market_price (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    vehicle_variant_id UUID NOT NULL REFERENCES vehicle_variant(id),
    price_amount NUMERIC(18, 2) NOT NULL,
    currency_code VARCHAR(3) NOT NULL DEFAULT 'VND',
    price_source VARCHAR(100),
    effective_from DATE NOT NULL,
    effective_to DATE,
    note TEXT,
    CONSTRAINT chk_vehicle_market_price_amount CHECK (price_amount > 0),
    CONSTRAINT chk_vehicle_market_price_date_range CHECK (effective_to IS NULL OR effective_to >= effective_from)
);

CREATE INDEX idx_vehicle_market_price_variant_id
ON vehicle_market_price(vehicle_variant_id);

CREATE INDEX idx_vehicle_market_price_effective_date
ON vehicle_market_price(effective_from, effective_to);

CREATE UNIQUE INDEX uq_vehicle_market_price_current
ON vehicle_market_price(vehicle_variant_id)
WHERE effective_to IS NULL;

-- =========================================================
-- 3. Asset and loan application link
-- =========================================================

CREATE TABLE asset (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    asset_code VARCHAR(50) NOT NULL,
    vehicle_variant_id UUID NOT NULL REFERENCES vehicle_variant(id),
    license_plate VARCHAR(20) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'AVAILABLE',
    CONSTRAINT uq_asset_code UNIQUE (asset_code),
    CONSTRAINT uq_asset_license_plate UNIQUE (license_plate),
    CONSTRAINT chk_asset_status CHECK (status IN ('AVAILABLE', 'PLEDGED', 'RELEASED', 'SETTLED'))
);

CREATE INDEX idx_asset_vehicle_variant_id
ON asset(vehicle_variant_id);

CREATE INDEX idx_asset_status
ON asset(status);

ALTER TABLE loan_application
ADD COLUMN asset_id UUID REFERENCES asset(id);

CREATE INDEX idx_loan_application_asset_id
ON loan_application(asset_id);

-- =========================================================
-- 4. Asset valuation
-- =========================================================

CREATE TABLE asset_deduction_type (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(50) NOT NULL,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    deduction_amount NUMERIC(18, 2) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    sort_order INT NOT NULL DEFAULT 0,
    CONSTRAINT uq_asset_deduction_type_code UNIQUE (code),
    CONSTRAINT chk_asset_deduction_type_amount CHECK (deduction_amount >= 0)
);

CREATE TABLE asset_valuation (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    asset_id UUID NOT NULL REFERENCES asset(id),
    market_price_amount NUMERIC(18, 2) NOT NULL,
    total_deduction_amount NUMERIC(18, 2) NOT NULL DEFAULT 0,
    final_value_amount NUMERIC(18, 2) NOT NULL,
    currency_code VARCHAR(3) NOT NULL DEFAULT 'VND',
    valuation_source VARCHAR(100),
    valued_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    valued_by VARCHAR(100),
    note TEXT,
    CONSTRAINT chk_asset_valuation_market_price_amount CHECK (market_price_amount > 0),
    CONSTRAINT chk_asset_valuation_total_deduction_amount CHECK (total_deduction_amount >= 0),
    CONSTRAINT chk_asset_valuation_final_value_amount CHECK (final_value_amount >= 0),
    CONSTRAINT chk_asset_valuation_final_not_greater_than_market CHECK (final_value_amount <= market_price_amount),
    CONSTRAINT chk_asset_valuation_deduction_not_greater_than_market CHECK (total_deduction_amount <= market_price_amount),
    CONSTRAINT chk_asset_valuation_final_matches_deduction CHECK (final_value_amount = market_price_amount - total_deduction_amount)
);

CREATE INDEX idx_asset_valuation_asset_id
ON asset_valuation(asset_id);

CREATE INDEX idx_asset_valuation_valued_at
ON asset_valuation(valued_at);

CREATE TABLE asset_valuation_deduction (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    asset_valuation_id UUID NOT NULL REFERENCES asset_valuation(id) ON DELETE CASCADE,
    deduction_type_id UUID NOT NULL REFERENCES asset_deduction_type(id),
    deduction_amount_snapshot NUMERIC(18, 2) NOT NULL,
    note TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_asset_valuation_deduction_amount_snapshot CHECK (deduction_amount_snapshot >= 0),
    CONSTRAINT uq_asset_valuation_deduction_type UNIQUE (asset_valuation_id, deduction_type_id)
);

CREATE INDEX idx_asset_valuation_deduction_valuation_id
ON asset_valuation_deduction(asset_valuation_id);

CREATE INDEX idx_asset_valuation_deduction_type_id
ON asset_valuation_deduction(deduction_type_id);

-- =========================================================
-- 5. Seed vehicle catalog and market price
-- =========================================================

INSERT INTO vehicle_type
(id, code, name, description, is_active, sort_order)
VALUES
('00000000-0000-0000-0000-000000004001', 'MOTORBIKE', 'Xe máy', 'Xe máy, xe tay ga, xe côn tay.', TRUE, 1),
('00000000-0000-0000-0000-000000004002', 'CAR', 'Ô tô', 'Ô tô cá nhân hoặc xe con.', TRUE, 2)
ON CONFLICT (code) DO NOTHING;

INSERT INTO vehicle_brand
(id, vehicle_type_id, code, name, is_active, sort_order)
VALUES
('00000000-0000-0000-0000-000000004101', '00000000-0000-0000-0000-000000004001', 'YAMAHA', 'Yamaha', TRUE, 1),
('00000000-0000-0000-0000-000000004102', '00000000-0000-0000-0000-000000004001', 'HONDA', 'Honda', TRUE, 2),
('00000000-0000-0000-0000-000000004103', '00000000-0000-0000-0000-000000004002', 'TOYOTA', 'Toyota', TRUE, 1),
('00000000-0000-0000-0000-000000004104', '00000000-0000-0000-0000-000000004002', 'MAZDA', 'Mazda', TRUE, 2)
ON CONFLICT (vehicle_type_id, code) DO NOTHING;

INSERT INTO vehicle_model
(id, vehicle_brand_id, code, name, is_active, sort_order)
VALUES
('00000000-0000-0000-0000-000000004201', '00000000-0000-0000-0000-000000004101', 'EXCITER_155', 'Exciter 155', TRUE, 1),
('00000000-0000-0000-0000-000000004202', '00000000-0000-0000-0000-000000004101', 'GRANDE', 'Grande', TRUE, 2),
('00000000-0000-0000-0000-000000004203', '00000000-0000-0000-0000-000000004102', 'SH_150', 'SH 150', TRUE, 1),
('00000000-0000-0000-0000-000000004204', '00000000-0000-0000-0000-000000004102', 'VISION', 'Vision', TRUE, 2),
('00000000-0000-0000-0000-000000004205', '00000000-0000-0000-0000-000000004103', 'VIOS', 'Vios', TRUE, 1),
('00000000-0000-0000-0000-000000004206', '00000000-0000-0000-0000-000000004103', 'CAMRY', 'Camry', TRUE, 2),
('00000000-0000-0000-0000-000000004207', '00000000-0000-0000-0000-000000004104', 'CX_5', 'CX-5', TRUE, 1)
ON CONFLICT (vehicle_brand_id, code) DO NOTHING;

INSERT INTO vehicle_version
(id, vehicle_model_id, code, name, is_active, sort_order)
VALUES
('00000000-0000-0000-0000-000000004301', '00000000-0000-0000-0000-000000004201', 'STANDARD', 'Tiêu chuẩn', TRUE, 1),
('00000000-0000-0000-0000-000000004302', '00000000-0000-0000-0000-000000004201', 'ABS', 'ABS', TRUE, 2),
('00000000-0000-0000-0000-000000004303', '00000000-0000-0000-0000-000000004201', 'GP', 'GP', TRUE, 3),
('00000000-0000-0000-0000-000000004304', '00000000-0000-0000-0000-000000004203', 'CBS', 'CBS', TRUE, 1),
('00000000-0000-0000-0000-000000004305', '00000000-0000-0000-0000-000000004203', 'ABS', 'ABS', TRUE, 2),
('00000000-0000-0000-0000-000000004306', '00000000-0000-0000-0000-000000004205', 'E_CVT', 'E CVT', TRUE, 1),
('00000000-0000-0000-0000-000000004307', '00000000-0000-0000-0000-000000004205', 'G_CVT', 'G CVT', TRUE, 2),
('00000000-0000-0000-0000-000000004308', '00000000-0000-0000-0000-000000004207', 'LUXURY', 'Luxury', TRUE, 1)
ON CONFLICT (vehicle_model_id, code) DO NOTHING;

INSERT INTO vehicle_year
(id, vehicle_version_id, manufacture_year, is_active, sort_order)
VALUES
('00000000-0000-0000-0000-000000004401', '00000000-0000-0000-0000-000000004302', 2021, TRUE, 3),
('00000000-0000-0000-0000-000000004402', '00000000-0000-0000-0000-000000004302', 2022, TRUE, 2),
('00000000-0000-0000-0000-000000004403', '00000000-0000-0000-0000-000000004302', 2023, TRUE, 1),
('00000000-0000-0000-0000-000000004404', '00000000-0000-0000-0000-000000004305', 2021, TRUE, 3),
('00000000-0000-0000-0000-000000004405', '00000000-0000-0000-0000-000000004305', 2022, TRUE, 2),
('00000000-0000-0000-0000-000000004406', '00000000-0000-0000-0000-000000004305', 2023, TRUE, 1),
('00000000-0000-0000-0000-000000004407', '00000000-0000-0000-0000-000000004307', 2020, TRUE, 3),
('00000000-0000-0000-0000-000000004408', '00000000-0000-0000-0000-000000004307', 2021, TRUE, 2),
('00000000-0000-0000-0000-000000004409', '00000000-0000-0000-0000-000000004307', 2022, TRUE, 1),
('00000000-0000-0000-0000-000000004410', '00000000-0000-0000-0000-000000004308', 2019, TRUE, 2),
('00000000-0000-0000-0000-000000004411', '00000000-0000-0000-0000-000000004308', 2020, TRUE, 1)
ON CONFLICT (vehicle_version_id, manufacture_year) DO NOTHING;

INSERT INTO vehicle_color
(id, code, name, is_active, sort_order)
VALUES
('00000000-0000-0000-0000-000000004501', 'BLACK', 'Đen', TRUE, 1),
('00000000-0000-0000-0000-000000004502', 'BLUE', 'Xanh', TRUE, 2),
('00000000-0000-0000-0000-000000004503', 'WHITE', 'Trắng', TRUE, 3),
('00000000-0000-0000-0000-000000004504', 'RED', 'Đỏ', TRUE, 4),
('00000000-0000-0000-0000-000000004505', 'SILVER', 'Bạc', TRUE, 5),
('00000000-0000-0000-0000-000000004506', 'GRAY', 'Xám', TRUE, 6)
ON CONFLICT (code) DO NOTHING;

INSERT INTO vehicle_variant
(id, vehicle_year_id, vehicle_color_id, code, name, is_active, sort_order)
VALUES
('00000000-0000-0000-0000-000000004601', '00000000-0000-0000-0000-000000004403', '00000000-0000-0000-0000-000000004501', 'YAMAHA_EXCITER_155_ABS_2023_BLACK', 'Yamaha Exciter 155 ABS 2023 Đen', TRUE, 1),
('00000000-0000-0000-0000-000000004602', '00000000-0000-0000-0000-000000004403', '00000000-0000-0000-0000-000000004502', 'YAMAHA_EXCITER_155_ABS_2023_BLUE', 'Yamaha Exciter 155 ABS 2023 Xanh', TRUE, 2),
('00000000-0000-0000-0000-000000004603', '00000000-0000-0000-0000-000000004403', '00000000-0000-0000-0000-000000004503', 'YAMAHA_EXCITER_155_ABS_2023_WHITE', 'Yamaha Exciter 155 ABS 2023 Trắng', TRUE, 3),
('00000000-0000-0000-0000-000000004604', '00000000-0000-0000-0000-000000004405', '00000000-0000-0000-0000-000000004501', 'HONDA_SH_150_ABS_2022_BLACK', 'Honda SH 150 ABS 2022 Đen', TRUE, 1),
('00000000-0000-0000-0000-000000004605', '00000000-0000-0000-0000-000000004405', '00000000-0000-0000-0000-000000004503', 'HONDA_SH_150_ABS_2022_WHITE', 'Honda SH 150 ABS 2022 Trắng', TRUE, 2),
('00000000-0000-0000-0000-000000004606', '00000000-0000-0000-0000-000000004407', '00000000-0000-0000-0000-000000004501', 'TOYOTA_VIOS_G_CVT_2020_BLACK', 'Toyota Vios G CVT 2020 Đen', TRUE, 1),
('00000000-0000-0000-0000-000000004607', '00000000-0000-0000-0000-000000004407', '00000000-0000-0000-0000-000000004503', 'TOYOTA_VIOS_G_CVT_2020_WHITE', 'Toyota Vios G CVT 2020 Trắng', TRUE, 2),
('00000000-0000-0000-0000-000000004608', '00000000-0000-0000-0000-000000004407', '00000000-0000-0000-0000-000000004505', 'TOYOTA_VIOS_G_CVT_2020_SILVER', 'Toyota Vios G CVT 2020 Bạc', TRUE, 3)
ON CONFLICT (vehicle_year_id, vehicle_color_id) DO NOTHING;

INSERT INTO vehicle_market_price
(id, vehicle_variant_id, price_amount, currency_code, price_source, effective_from, effective_to, note)
VALUES
('00000000-0000-0000-0000-000000004701', '00000000-0000-0000-0000-000000004601', 43000000.00, 'VND', 'DEMO_PRICE_LIST', DATE '2026-01-01', NULL, 'Exciter 155 ABS 2023 màu đen.'),
('00000000-0000-0000-0000-000000004702', '00000000-0000-0000-0000-000000004602', 45000000.00, 'VND', 'DEMO_PRICE_LIST', DATE '2026-01-01', NULL, 'Exciter 155 ABS 2023 màu xanh có giá cao hơn.'),
('00000000-0000-0000-0000-000000004703', '00000000-0000-0000-0000-000000004603', 42000000.00, 'VND', 'DEMO_PRICE_LIST', DATE '2026-01-01', NULL, 'Exciter 155 ABS 2023 màu trắng.'),
('00000000-0000-0000-0000-000000004704', '00000000-0000-0000-0000-000000004604', 88000000.00, 'VND', 'DEMO_PRICE_LIST', DATE '2026-01-01', NULL, 'SH 150 ABS 2022 màu đen.'),
('00000000-0000-0000-0000-000000004705', '00000000-0000-0000-0000-000000004605', 90000000.00, 'VND', 'DEMO_PRICE_LIST', DATE '2026-01-01', NULL, 'SH 150 ABS 2022 màu trắng.'),
('00000000-0000-0000-0000-000000004706', '00000000-0000-0000-0000-000000004606', 295000000.00, 'VND', 'DEMO_PRICE_LIST', DATE '2026-01-01', NULL, 'Vios G CVT 2020 màu đen.'),
('00000000-0000-0000-0000-000000004707', '00000000-0000-0000-0000-000000004607', 300000000.00, 'VND', 'DEMO_PRICE_LIST', DATE '2026-01-01', NULL, 'Vios G CVT 2020 màu trắng.'),
('00000000-0000-0000-0000-000000004708', '00000000-0000-0000-0000-000000004608', 298000000.00, 'VND', 'DEMO_PRICE_LIST', DATE '2026-01-01', NULL, 'Vios G CVT 2020 màu bạc.')
ON CONFLICT (vehicle_variant_id)
WHERE effective_to IS NULL
DO NOTHING;

-- Demo assets are not customer-owned directly. They are linked from loan_application.
INSERT INTO asset
(id, asset_code, vehicle_variant_id, license_plate, status)
VALUES
('50000000-0000-0000-0000-000000000001', 'AST-2026-000001', '00000000-0000-0000-0000-000000004602', '29A12345', 'AVAILABLE'),
('50000000-0000-0000-0000-000000000002', 'AST-2026-000002', '00000000-0000-0000-0000-000000004604', '30B67890', 'PLEDGED'),
('50000000-0000-0000-0000-000000000003', 'AST-2026-000003', '00000000-0000-0000-0000-000000004607', '30A55555', 'RELEASED')
ON CONFLICT (asset_code) DO NOTHING;

UPDATE loan_application
SET asset_id = '50000000-0000-0000-0000-000000000001'
WHERE loan_application_code = 'APP-2026-000001'
  AND asset_id IS NULL;

-- =========================================================
-- 6. Seed deduction types
-- =========================================================

INSERT INTO asset_deduction_type
(code, name, description, deduction_amount, is_active, sort_order)
VALUES
('OLD_VEHICLE', 'Xe cũ', 'Giảm trừ do xe đã cũ hoặc đời xe thấp hơn mặt bằng định giá.', 3000000, TRUE, 10),
('PHYSICAL_DAMAGE', 'Hư hỏng ngoại thất', 'Giảm trừ do trầy xước, móp méo, va chạm hoặc hư hỏng bên ngoài.', 5000000, TRUE, 20),
('ENGINE_ISSUE', 'Lỗi động cơ', 'Giảm trừ do động cơ yếu, đã sửa chữa lớn hoặc có dấu hiệu hư hỏng.', 8000000, TRUE, 30),
('HIGH_MILEAGE', 'Số km cao', 'Giảm trừ do xe đã sử dụng nhiều, số km cao hơn mức thông thường.', 3000000, TRUE, 50),
('LOW_LIQUIDITY_COLOR', 'Màu khó thanh khoản', 'Giảm trừ do màu xe khó bán lại hoặc ít được thị trường ưa chuộng.', 1000000, TRUE, 60),
('MODIFIED_VEHICLE', 'Xe đã độ/chỉnh sửa', 'Giảm trừ do xe đã thay đổi kết cấu, độ máy, độ ngoại hình hoặc phụ kiện quan trọng.', 4000000, TRUE, 70),
('OTHER', 'Khác', 'Yếu tố giảm trừ khác không thuộc các nhóm trên.', 0, TRUE, 999)
ON CONFLICT (code) DO UPDATE
SET
    name = EXCLUDED.name,
    description = EXCLUDED.description,
    deduction_amount = EXCLUDED.deduction_amount,
    is_active = EXCLUDED.is_active,
    sort_order = EXCLUDED.sort_order;
