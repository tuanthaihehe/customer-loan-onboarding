-- Customer Loan Onboarding - Add Vehicle Identifier Fields to Asset
-- PostgreSQL dialect
-- Version: V15
--
-- Scope:
-- - Add vehicle frame number, engine number, and registration issue date to asset.
--
-- Design notes:
-- - asset currently stores license_plate only.
-- - frame_number and engine_number identify the physical vehicle.
-- - registration_issue_date stores the issue date from the vehicle registration document.
-- - The fields are nullable because old/draft asset records may not have these values yet.

ALTER TABLE asset
ADD COLUMN IF NOT EXISTS frame_number VARCHAR(100),
ADD COLUMN IF NOT EXISTS engine_number VARCHAR(100),
ADD COLUMN IF NOT EXISTS registration_issue_date DATE;

CREATE UNIQUE INDEX IF NOT EXISTS uq_asset_frame_number
ON asset(frame_number)
WHERE frame_number IS NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uq_asset_engine_number
ON asset(engine_number)
WHERE engine_number IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_asset_registration_issue_date
ON asset(registration_issue_date);
