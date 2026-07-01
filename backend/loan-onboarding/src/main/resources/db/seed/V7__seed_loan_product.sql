-- Customer Loan Onboarding - Seed Loan Product
-- PostgreSQL dialect
-- Version: V7 seed
--
-- Scope:
-- - Seed score grades.
-- - Seed loan products.
-- - Seed product-purpose, product-vehicle-type, product-term, and product-score-grade mappings.
--
-- Requirements:
-- - Must be run after:
--   V8__add_loan_purpose_catalog.sql + V5__seed_loan_purpose.sql
--   V9__add_loan_term_catalog.sql + V6__seed_loan_term.sql
--   V10__add_loan_product_catalog.sql
--   vehicle_type seed data containing MOTORBIKE and CAR.
--
-- Notes:
-- - Product rows use PERSONAL_CONSUMPTION to match loan_purpose.code.
-- - XM_STANDARD applies to all loan purposes, so no row is inserted into loan_product_purpose for it.
-- - This seed is idempotent.

INSERT INTO score_grade (
    code,
    name,
    description,
    is_active,
    sort_order
)
VALUES
    ('A', 'Hạng A', 'Hạng điểm tốt nhất.', TRUE, 10),
    ('B', 'Hạng B', 'Hạng điểm tốt.', TRUE, 20),
    ('C', 'Hạng C', 'Hạng điểm trung bình.', TRUE, 30),
    ('D', 'Hạng D', 'Hạng điểm rủi ro cao.', TRUE, 40)
ON CONFLICT (code) DO UPDATE
SET
    name = EXCLUDED.name,
    description = EXCLUDED.description,
    is_active = EXCLUDED.is_active,
    sort_order = EXCLUDED.sort_order;

INSERT INTO loan_product (
    product_code,
    product_name,
    applies_to_all_loan_purposes,
    min_loan_amount,
    max_loan_amount,
    max_ltv_percent,
    monthly_interest_rate_percent,
    is_active,
    sort_order
)
VALUES
    ('XM_STANDARD', 'Xe máy tiêu chuẩn', TRUE, 3000000, 15000000, 60.00, 3.20, TRUE, 10),
    ('XM_FLEX', 'Xe máy linh hoạt', FALSE, 5000000, 20000000, 70.00, 2.90, TRUE, 20),
    ('XM_PREFER', 'Xe máy ưu đãi', FALSE, 7000000, 30000000, 75.00, 2.80, TRUE, 30),
    ('XM_SHORT', 'Xe máy ngắn hạn', FALSE, 3000000, 9000000, 50.00, 2.70, TRUE, 40),
    ('XM_BUSINESS', 'Xe máy kinh doanh', FALSE, 8000000, 25000000, 80.00, 3.40, TRUE, 50),
    ('XM_HIGH_LIMIT', 'Xe máy hạn mức cao', FALSE, 15000000, 40000000, 85.00, 4.20, TRUE, 60),
    ('CAR_STANDARD', 'Ô tô tiêu chuẩn', FALSE, 20000000, 100000000, 70.00, 2.50, TRUE, 70)
ON CONFLICT (product_code) DO UPDATE
SET
    product_name = EXCLUDED.product_name,
    applies_to_all_loan_purposes = EXCLUDED.applies_to_all_loan_purposes,
    min_loan_amount = EXCLUDED.min_loan_amount,
    max_loan_amount = EXCLUDED.max_loan_amount,
    max_ltv_percent = EXCLUDED.max_ltv_percent,
    monthly_interest_rate_percent = EXCLUDED.monthly_interest_rate_percent,
    is_active = EXCLUDED.is_active,
    sort_order = EXCLUDED.sort_order;

DELETE FROM loan_product_purpose lpp
USING loan_product lp
WHERE lpp.loan_product_id = lp.id
  AND lp.product_code IN (
      'XM_STANDARD', 'XM_FLEX', 'XM_PREFER', 'XM_SHORT',
      'XM_BUSINESS', 'XM_HIGH_LIMIT', 'CAR_STANDARD'
  );

DELETE FROM loan_product_vehicle_type lpvt
USING loan_product lp
WHERE lpvt.loan_product_id = lp.id
  AND lp.product_code IN (
      'XM_STANDARD', 'XM_FLEX', 'XM_PREFER', 'XM_SHORT',
      'XM_BUSINESS', 'XM_HIGH_LIMIT', 'CAR_STANDARD'
  );

DELETE FROM loan_product_term lpt
USING loan_product lp
WHERE lpt.loan_product_id = lp.id
  AND lp.product_code IN (
      'XM_STANDARD', 'XM_FLEX', 'XM_PREFER', 'XM_SHORT',
      'XM_BUSINESS', 'XM_HIGH_LIMIT', 'CAR_STANDARD'
  );

DELETE FROM loan_product_score_grade lpsg
USING loan_product lp
WHERE lpsg.loan_product_id = lp.id
  AND lp.product_code IN (
      'XM_STANDARD', 'XM_FLEX', 'XM_PREFER', 'XM_SHORT',
      'XM_BUSINESS', 'XM_HIGH_LIMIT', 'CAR_STANDARD'
  );

-- XM_STANDARD applies to all loan purposes, so it is intentionally not mapped here.
INSERT INTO loan_product_purpose (
    loan_product_id,
    loan_purpose_id
)
SELECT lp.id, lpur.id
FROM (
    VALUES
        ('XM_FLEX', 'PERSONAL_CONSUMPTION'),
        ('XM_PREFER', 'PERSONAL_CONSUMPTION'),
        ('XM_SHORT', 'PERSONAL_CONSUMPTION'),
        ('XM_BUSINESS', 'BUSINESS'),
        ('XM_HIGH_LIMIT', 'PERSONAL_CONSUMPTION'),
        ('CAR_STANDARD', 'PERSONAL_CONSUMPTION')
) AS m(product_code, loan_purpose_code)
JOIN loan_product lp
    ON lp.product_code = m.product_code
JOIN loan_purpose lpur
    ON lpur.code = m.loan_purpose_code
ON CONFLICT DO NOTHING;

INSERT INTO loan_product_vehicle_type (
    loan_product_id,
    vehicle_type_id
)
SELECT lp.id, vt.id
FROM (
    VALUES
        ('XM_STANDARD', 'MOTORBIKE'),
        ('XM_FLEX', 'MOTORBIKE'),
        ('XM_PREFER', 'MOTORBIKE'),
        ('XM_SHORT', 'MOTORBIKE'),
        ('XM_BUSINESS', 'MOTORBIKE'),
        ('XM_HIGH_LIMIT', 'MOTORBIKE'),
        ('CAR_STANDARD', 'CAR')
) AS m(product_code, vehicle_type_code)
JOIN loan_product lp
    ON lp.product_code = m.product_code
JOIN vehicle_type vt
    ON vt.code = m.vehicle_type_code
ON CONFLICT DO NOTHING;

INSERT INTO loan_product_term (
    loan_product_id,
    loan_term_id
)
SELECT lp.id, lt.id
FROM (
    VALUES
        ('XM_STANDARD', 6),
        ('XM_STANDARD', 12),
        ('XM_STANDARD', 18),
        ('XM_STANDARD', 24),
        ('XM_FLEX', 12),
        ('XM_FLEX', 24),
        ('XM_PREFER', 6),
        ('XM_PREFER', 12),
        ('XM_SHORT', 6),
        ('XM_BUSINESS', 6),
        ('XM_BUSINESS', 12),
        ('XM_BUSINESS', 18),
        ('XM_HIGH_LIMIT', 12),
        ('XM_HIGH_LIMIT', 18),
        ('XM_HIGH_LIMIT', 24),
        ('CAR_STANDARD', 12),
        ('CAR_STANDARD', 24)
) AS m(product_code, term_months)
JOIN loan_product lp
    ON lp.product_code = m.product_code
JOIN loan_term lt
    ON lt.term_months = m.term_months
ON CONFLICT DO NOTHING;

INSERT INTO loan_product_score_grade (
    loan_product_id,
    score_grade_id
)
SELECT lp.id, sg.id
FROM (
    VALUES
        ('XM_STANDARD', 'A'),
        ('XM_STANDARD', 'B'),
        ('XM_STANDARD', 'C'),
        ('XM_FLEX', 'A'),
        ('XM_FLEX', 'B'),
        ('XM_PREFER', 'A'),
        ('XM_SHORT', 'A'),
        ('XM_SHORT', 'B'),
        ('XM_SHORT', 'C'),
        ('XM_BUSINESS', 'A'),
        ('XM_BUSINESS', 'B'),
        ('XM_HIGH_LIMIT', 'A'),
        ('XM_HIGH_LIMIT', 'B'),
        ('CAR_STANDARD', 'A'),
        ('CAR_STANDARD', 'B'),
        ('CAR_STANDARD', 'C')
) AS m(product_code, score_grade_code)
JOIN loan_product lp
    ON lp.product_code = m.product_code
JOIN score_grade sg
    ON sg.code = m.score_grade_code
ON CONFLICT DO NOTHING;
