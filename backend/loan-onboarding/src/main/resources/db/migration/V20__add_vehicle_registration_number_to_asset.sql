-- Customer Loan Onboarding - Add Vehicle Registration Number
-- PostgreSQL dialect
-- Version: V20
--
-- Scope:
-- - Add registration_number to asset for vehicle registration document information.
--
-- Business context:
-- - license_plate stores the vehicle plate number from the preliminary asset step.
-- - registration_number stores the number printed on the vehicle registration document.
-- - registration_issue_date already exists from V15.

ALTER TABLE asset
ADD COLUMN IF NOT EXISTS registration_number VARCHAR(100);

CREATE UNIQUE INDEX IF NOT EXISTS uq_asset_registration_number
ON asset(registration_number)
WHERE registration_number IS NOT NULL;
