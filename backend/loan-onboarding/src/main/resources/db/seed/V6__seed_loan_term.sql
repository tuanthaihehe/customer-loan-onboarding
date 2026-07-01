-- Customer Loan Onboarding - Seed Loan Term
-- PostgreSQL dialect
-- Version: V6 seed
--
-- Scope:
-- - Seed master data for loan term dropdown.
--
-- This file is idempotent. It can be run multiple times safely.
-- It must be run after V9__add_loan_term_catalog.sql.

INSERT INTO loan_term (
    code,
    term_months,
    name,
    description,
    is_active,
    sort_order
)
VALUES
    (
        'TERM_3M',
        3,
        '3 tháng',
        'Kỳ hạn vay 3 tháng.',
        TRUE,
        10
    ),
    (
        'TERM_6M',
        6,
        '6 tháng',
        'Kỳ hạn vay 6 tháng.',
        TRUE,
        20
    ),
    (
        'TERM_9M',
        9,
        '9 tháng',
        'Kỳ hạn vay 9 tháng.',
        TRUE,
        30
    ),
    (
        'TERM_12M',
        12,
        '12 tháng',
        'Kỳ hạn vay 12 tháng.',
        TRUE,
        40
    ),
    (
        'TERM_18M',
        18,
        '18 tháng',
        'Kỳ hạn vay 18 tháng.',
        TRUE,
        50
    ),
    (
        'TERM_24M',
        24,
        '24 tháng',
        'Kỳ hạn vay 24 tháng.',
        TRUE,
        60
    )
ON CONFLICT (code) DO UPDATE
SET
    term_months = EXCLUDED.term_months,
    name = EXCLUDED.name,
    description = EXCLUDED.description,
    is_active = EXCLUDED.is_active,
    sort_order = EXCLUDED.sort_order;
