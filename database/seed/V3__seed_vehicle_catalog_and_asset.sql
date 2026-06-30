-- Customer Loan Onboarding - Vehicle Catalog and Asset Seed
-- PostgreSQL dialect
--
-- Scope:
-- - Seed vehicle catalog data for dropdowns.
-- - Seed vehicle variants where color affects market price.
-- - Seed current market prices for demo variants.
-- - Seed demo assets for existing demo customers when they exist.
--
-- This seed assumes:
-- - V1__init_schema.sql has already created customer.
-- - V4__add_vehicle_catalog_and_asset.sql has already created vehicle/asset tables.
--
-- This seed is re-runnable through ON CONFLICT DO NOTHING.

-- =========================================================
-- 1. Vehicle types
-- =========================================================

INSERT INTO vehicle_type
(id, code, name, description, is_active, sort_order)
VALUES
('00000000-0000-0000-0000-000000004001', 'MOTORBIKE', 'Xe máy', 'Xe máy, xe tay ga, xe côn tay.', TRUE, 1),
('00000000-0000-0000-0000-000000004002', 'CAR', 'Ô tô', 'Ô tô cá nhân hoặc xe con.', TRUE, 2)
ON CONFLICT (code) DO NOTHING;

-- =========================================================
-- 2. Vehicle brands
-- =========================================================

INSERT INTO vehicle_brand
(id, vehicle_type_id, code, name, is_active, sort_order)
VALUES
('00000000-0000-0000-0000-000000004101', '00000000-0000-0000-0000-000000004001', 'YAMAHA', 'Yamaha', TRUE, 1),
('00000000-0000-0000-0000-000000004102', '00000000-0000-0000-0000-000000004001', 'HONDA', 'Honda', TRUE, 2),
('00000000-0000-0000-0000-000000004103', '00000000-0000-0000-0000-000000004002', 'TOYOTA', 'Toyota', TRUE, 1),
('00000000-0000-0000-0000-000000004104', '00000000-0000-0000-0000-000000004002', 'MAZDA', 'Mazda', TRUE, 2)
ON CONFLICT (vehicle_type_id, code) DO NOTHING;

-- =========================================================
-- 3. Vehicle models
-- =========================================================

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

-- =========================================================
-- 4. Vehicle versions
-- =========================================================

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

-- =========================================================
-- 5. Vehicle years
-- =========================================================

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

-- =========================================================
-- 6. Vehicle colors
-- =========================================================

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

-- =========================================================
-- 7. Vehicle variants
-- =========================================================
-- Color is part of vehicle_variant because it affects market price.

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

-- =========================================================
-- 8. Vehicle market prices
-- =========================================================
-- Demo prices show that color can affect market price.

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

-- =========================================================
-- 9. Demo assets
-- =========================================================
-- Demo assets are inserted only if the referenced demo customers exist.

INSERT INTO asset
(asset_code, customer_id, vehicle_variant_id, license_plate, status)
SELECT
    'AST-2026-000001',
    c.id,
    '00000000-0000-0000-0000-000000004602',
    '29A12345',
    'AVAILABLE'
FROM customer c
WHERE c.customer_code = 'CUS-2026-000001'
ON CONFLICT (asset_code) DO NOTHING;

INSERT INTO asset
(asset_code, customer_id, vehicle_variant_id, license_plate, status)
SELECT
    'AST-2026-000002',
    c.id,
    '00000000-0000-0000-0000-000000004604',
    '30B67890',
    'PLEDGED'
FROM customer c
WHERE c.customer_code = 'CUS-2026-000002'
ON CONFLICT (asset_code) DO NOTHING;

INSERT INTO asset
(asset_code, customer_id, vehicle_variant_id, license_plate, status)
SELECT
    'AST-2026-000003',
    c.id,
    '00000000-0000-0000-0000-000000004607',
    '30A55555',
    'RELEASED'
FROM customer c
WHERE c.customer_code = 'CUS-2026-000003'
ON CONFLICT (asset_code) DO NOTHING;
