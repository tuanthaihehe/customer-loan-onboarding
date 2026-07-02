-- Customer Loan Onboarding - Seed Mock Score Grade Rule
-- PostgreSQL dialect
-- Version: V9 seed
--
-- Scope:
-- - Seed diverse dummy rules for mock customer score grading.
--
-- Requirements:
-- - Must be run after V16__add_mock_score_grade_rule.sql.
-- - score_grade must contain A, B, C, D.
--
-- Rule matching idea:
-- - Monthly income, requested loan amount, and LTV are compared to ranges.
-- - NULL min/max means unbounded.
-- - Backend/query should select the first matched rule by sort_order.
--
-- Important:
-- - These rules are dummy/demo data, not real credit policy.

INSERT INTO mock_score_grade_rule (
    rule_code,
    rule_name,
    description,
    min_monthly_income_amount,
    max_monthly_income_amount,
    min_requested_amount,
    max_requested_amount,
    min_ltv_percent,
    max_ltv_percent,
    score_grade_id,
    is_active,
    sort_order
)
SELECT
    v.rule_code,
    v.rule_name,
    v.description,
    v.min_monthly_income_amount,
    v.max_monthly_income_amount,
    v.min_requested_amount,
    v.max_requested_amount,
    v.min_ltv_percent,
    v.max_ltv_percent,
    sg.id,
    TRUE,
    v.sort_order
FROM (
    VALUES
        -- Grade A: high income and low/moderate LTV
        (
            'A_HIGH_INCOME_LOW_LTV_SMALL_LOAN',
            'Hạng A - thu nhập cao, LTV thấp, khoản vay nhỏ',
            'Thu nhập rất tốt, khoản vay nhỏ và tỷ lệ vay trên tài sản thấp.',
            30000000::numeric, NULL::numeric,
            NULL::numeric, 10000000::numeric,
            NULL::numeric, 45.00::numeric,
            'A',
            10
        ),
        (
            'A_HIGH_INCOME_LOW_LTV_MEDIUM_LOAN',
            'Hạng A - thu nhập cao, LTV thấp, khoản vay trung bình',
            'Thu nhập tốt, khoản vay trung bình và LTV thấp.',
            25000000::numeric, NULL::numeric,
            10000000::numeric, 25000000::numeric,
            NULL::numeric, 50.00::numeric,
            'A',
            20
        ),
        (
            'A_VERY_HIGH_INCOME_MODERATE_LTV',
            'Hạng A - thu nhập rất cao, LTV vừa',
            'Thu nhập rất cao nên có thể chấp nhận LTV ở mức vừa.',
            40000000::numeric, NULL::numeric,
            NULL::numeric, 40000000::numeric,
            50.01::numeric, 60.00::numeric,
            'A',
            30
        ),
        (
            'A_GOOD_INCOME_TINY_LOAN',
            'Hạng A - thu nhập tốt, khoản vay rất nhỏ',
            'Khoản vay rất nhỏ so với thu nhập.',
            18000000::numeric, NULL::numeric,
            NULL::numeric, 7000000::numeric,
            NULL::numeric, 55.00::numeric,
            'A',
            40
        ),

        -- Grade B: decent income and moderate LTV
        (
            'B_GOOD_INCOME_LOW_LTV',
            'Hạng B - thu nhập khá, LTV thấp',
            'Thu nhập khá và tỷ lệ vay trên tài sản thấp.',
            15000000::numeric, 29999999::numeric,
            NULL::numeric, 20000000::numeric,
            NULL::numeric, 50.00::numeric,
            'B',
            100
        ),
        (
            'B_GOOD_INCOME_MEDIUM_LTV',
            'Hạng B - thu nhập khá, LTV trung bình',
            'Thu nhập khá và LTV trung bình.',
            15000000::numeric, NULL::numeric,
            NULL::numeric, 30000000::numeric,
            50.01::numeric, 65.00::numeric,
            'B',
            110
        ),
        (
            'B_AVERAGE_INCOME_LOW_LTV',
            'Hạng B - thu nhập trung bình, LTV thấp',
            'Thu nhập trung bình nhưng LTV thấp.',
            10000000::numeric, 14999999::numeric,
            NULL::numeric, 15000000::numeric,
            NULL::numeric, 50.00::numeric,
            'B',
            120
        ),
        (
            'B_HIGH_INCOME_HIGHER_LOAN',
            'Hạng B - thu nhập cao, khoản vay cao',
            'Thu nhập cao, khoản vay lớn nhưng LTV vẫn kiểm soát được.',
            25000000::numeric, NULL::numeric,
            25000001::numeric, 50000000::numeric,
            NULL::numeric, 70.00::numeric,
            'B',
            130
        ),
        (
            'B_STABLE_MEDIUM_LOAN',
            'Hạng B - điều kiện ổn định, khoản vay vừa',
            'Thu nhập và khoản vay ở mức ổn định.',
            12000000::numeric, 24999999::numeric,
            7000001::numeric, 20000000::numeric,
            45.01::numeric, 65.00::numeric,
            'B',
            140
        ),

        -- Grade C: acceptable but weaker profile
        (
            'C_AVERAGE_INCOME_MEDIUM_LTV',
            'Hạng C - thu nhập trung bình, LTV trung bình cao',
            'Thu nhập trung bình và LTV tương đối cao.',
            8000000::numeric, 14999999::numeric,
            NULL::numeric, 20000000::numeric,
            50.01::numeric, 75.00::numeric,
            'C',
            200
        ),
        (
            'C_LOW_INCOME_LOW_LTV',
            'Hạng C - thu nhập thấp, LTV thấp',
            'Thu nhập thấp nhưng tỷ lệ vay trên tài sản thấp.',
            5000000::numeric, 7999999::numeric,
            NULL::numeric, 10000000::numeric,
            NULL::numeric, 50.00::numeric,
            'C',
            210
        ),
        (
            'C_GOOD_INCOME_HIGH_LTV',
            'Hạng C - thu nhập khá, LTV cao',
            'Thu nhập khá nhưng LTV cao.',
            12000000::numeric, NULL::numeric,
            NULL::numeric, 30000000::numeric,
            65.01::numeric, 80.00::numeric,
            'C',
            220
        ),
        (
            'C_MEDIUM_INCOME_MEDIUM_LOAN',
            'Hạng C - thu nhập vừa, khoản vay vừa',
            'Hồ sơ ở mức chấp nhận được.',
            7000000::numeric, 11999999::numeric,
            7000001::numeric, 18000000::numeric,
            45.01::numeric, 70.00::numeric,
            'C',
            230
        ),
        (
            'C_SMALL_LOAN_WEAK_INCOME',
            'Hạng C - khoản vay nhỏ, thu nhập yếu',
            'Thu nhập yếu nhưng khoản vay nhỏ.',
            4000000::numeric, 6999999::numeric,
            NULL::numeric, 6000000::numeric,
            NULL::numeric, 60.00::numeric,
            'C',
            240
        ),

        -- Grade D: high risk / fallback-like rules
        (
            'D_VERY_HIGH_LTV',
            'Hạng D - LTV rất cao',
            'Tỷ lệ vay trên tài sản rất cao.',
            NULL::numeric, NULL::numeric,
            NULL::numeric, NULL::numeric,
            80.01::numeric, NULL::numeric,
            'D',
            300
        ),
        (
            'D_LOW_INCOME_MEDIUM_LTV',
            'Hạng D - thu nhập thấp, LTV trung bình cao',
            'Thu nhập thấp và LTV không thấp.',
            NULL::numeric, 4999999::numeric,
            NULL::numeric, NULL::numeric,
            50.01::numeric, NULL::numeric,
            'D',
            310
        ),
        (
            'D_LOW_INCOME_HIGH_LOAN',
            'Hạng D - thu nhập thấp, khoản vay cao',
            'Khoản vay cao so với mức thu nhập thấp.',
            NULL::numeric, 7999999::numeric,
            15000001::numeric, NULL::numeric,
            NULL::numeric, NULL::numeric,
            'D',
            320
        ),
        (
            'D_UNKNOWN_LOW_INCOME',
            'Hạng D - thu nhập rất thấp',
            'Thu nhập rất thấp, đánh giá rủi ro cao.',
            NULL::numeric, 3999999::numeric,
            NULL::numeric, NULL::numeric,
            NULL::numeric, NULL::numeric,
            'D',
            330
        ),
        (
            'D_LARGE_LOAN_MODERATE_INCOME',
            'Hạng D - khoản vay lớn, thu nhập chưa tương xứng',
            'Khoản vay lớn trong khi thu nhập chưa đủ mạnh.',
            NULL::numeric, 14999999::numeric,
            30000001::numeric, NULL::numeric,
            NULL::numeric, NULL::numeric,
            'D',
            340
        ),

        -- Broad catch-all rules. Keep at the bottom.
        (
            'B_DEFAULT_STRONG_PROFILE',
            'Hạng B - mặc định cho hồ sơ tốt',
            'Rule mặc định cho hồ sơ có thu nhập tốt và LTV chưa cao.',
            15000000::numeric, NULL::numeric,
            NULL::numeric, NULL::numeric,
            NULL::numeric, 70.00::numeric,
            'B',
            900
        ),
        (
            'C_DEFAULT_ACCEPTABLE_PROFILE',
            'Hạng C - mặc định cho hồ sơ chấp nhận được',
            'Rule mặc định cho hồ sơ đủ dữ liệu nhưng không vào A/B/D.',
            5000000::numeric, NULL::numeric,
            NULL::numeric, NULL::numeric,
            NULL::numeric, 80.00::numeric,
            'C',
            910
        ),
        (
            'D_DEFAULT_RISK_PROFILE',
            'Hạng D - mặc định rủi ro',
            'Rule cuối cho các hồ sơ còn lại.',
            NULL::numeric, NULL::numeric,
            NULL::numeric, NULL::numeric,
            NULL::numeric, NULL::numeric,
            'D',
            999
        )
) AS v(
    rule_code,
    rule_name,
    description,
    min_monthly_income_amount,
    max_monthly_income_amount,
    min_requested_amount,
    max_requested_amount,
    min_ltv_percent,
    max_ltv_percent,
    score_grade_code,
    sort_order
)
JOIN score_grade sg
    ON sg.code = v.score_grade_code
ON CONFLICT (rule_code) DO UPDATE
SET
    rule_name = EXCLUDED.rule_name,
    description = EXCLUDED.description,
    min_monthly_income_amount = EXCLUDED.min_monthly_income_amount,
    max_monthly_income_amount = EXCLUDED.max_monthly_income_amount,
    min_requested_amount = EXCLUDED.min_requested_amount,
    max_requested_amount = EXCLUDED.max_requested_amount,
    min_ltv_percent = EXCLUDED.min_ltv_percent,
    max_ltv_percent = EXCLUDED.max_ltv_percent,
    score_grade_id = EXCLUDED.score_grade_id,
    is_active = EXCLUDED.is_active,
    sort_order = EXCLUDED.sort_order;
