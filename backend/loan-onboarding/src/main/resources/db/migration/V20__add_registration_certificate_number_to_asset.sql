-- Customer Loan Onboarding - Add Vehicle Registration Number to Asset
-- PostgreSQL dialect
-- Version: V20
--
-- Scope:
-- - Add vehicle registration certificate number to asset.
--
-- Business meaning:
-- - registration_certificate_number is the number/code printed on the vehicle
--   registration document, also commonly called the vehicle registration/cavet number.
-- - This is different from license_plate.
--
-- Design notes:
-- - Nullable because existing/draft asset records may not have this information yet.
-- - Unique nullable index prevents duplicate registration certificate numbers when provided.

ALTER TABLE asset
ADD COLUMN IF NOT EXISTS registration_certificate_number VARCHAR(100);

CREATE UNIQUE INDEX IF NOT EXISTS uq_asset_registration_certificate_number
ON asset(registration_certificate_number)
WHERE registration_certificate_number IS NOT NULL;
