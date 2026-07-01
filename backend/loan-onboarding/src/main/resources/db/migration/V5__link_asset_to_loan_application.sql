-- Customer Loan Onboarding - Add Asset Link to Loan Application
-- PostgreSQL dialect
-- Version: V5
--
-- Scope:
-- - Link loan_application to asset through nullable asset_id.
-- - Remove customer_id from asset because ownership/context is derived through loan_application.customer_id.
--
-- Design notes:
-- - A draft loan application can be created before an asset is selected.
--   Therefore loan_application.asset_id must be nullable.
-- - Asset is not modeled as a customer-owned master object in this module.
--   Asset exists in the context of a loan application.
-- - The customer of an asset can be resolved through:
--     asset -> loan_application -> customer
-- - This migration does not add UNIQUE(asset_id), because the rule
--   "one asset cannot be used by more than one active application"
--   depends on loan_application lifecycle state and should be checked by backend logic
--   or a later database design if needed.

-- =========================================================
-- 1. Remove customer_id from asset
-- =========================================================
-- V4 originally created asset.customer_id.
-- This module now treats asset as being attached to loan_application instead.
-- Drop the foreign key and column if they exist.

ALTER TABLE asset
DROP COLUMN IF EXISTS customer_id;

-- =========================================================
-- 2. Add nullable asset_id to loan_application
-- =========================================================
-- asset_id is nullable because a draft application may be created before asset selection.

ALTER TABLE loan_application
ADD COLUMN IF NOT EXISTS asset_id UUID REFERENCES asset(id);

CREATE INDEX IF NOT EXISTS idx_loan_application_asset_id
ON loan_application(asset_id);

-- =========================================================
-- 3. Notes for backend/service layer
-- =========================================================
-- When creating a draft loan application:
--   loan_application.asset_id = NULL
--
-- When user selects an asset:
--   UPDATE loan_application
--   SET asset_id = :asset_id
--   WHERE loan_application_code = :loan_application_code;
--
-- To check whether an asset is already attached to a non-terminal application:
--
--   SELECT la.*
--   FROM loan_application la
--   JOIN loan_application_state s ON s.id = la.current_state_id
--   WHERE la.asset_id = :asset_id
--     AND s.is_terminal = FALSE;
--
-- If the query returns any row, backend should not allow the asset to be attached
-- to another active/non-terminal loan application.
