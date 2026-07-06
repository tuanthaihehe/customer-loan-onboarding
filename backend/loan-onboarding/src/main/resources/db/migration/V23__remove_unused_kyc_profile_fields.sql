-- Customer Loan Onboarding - Remove unused KYC profile fields
-- PostgreSQL dialect
-- Version: V23
--
-- Scope:
-- - Remove KYC fields that are no longer captured/stored.

ALTER TABLE kyc_profile
    DROP CONSTRAINT IF EXISTS chk_kyc_face_authenticity_score;

ALTER TABLE kyc_profile
    DROP COLUMN IF EXISTS blacklist_check_result,
    DROP COLUMN IF EXISTS phone_otp_verification_result,
    DROP COLUMN IF EXISTS face_authenticity_score;
