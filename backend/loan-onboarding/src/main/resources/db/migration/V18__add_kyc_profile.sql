-- Customer Loan Onboarding - Add KYC Profile
-- PostgreSQL dialect
-- Version: V18
--
-- Scope:
-- - Create kyc_profile table.
--
-- Business context:
-- - KYC profile records the latest identity verification and trust signals
--   for a customer during onboarding.
-- - It is created after customer is found/created and identification checks begin.
-- - loan_application may not exist yet at that time.
-- - When a loan application is created, the KYC profile can be linked to it.
--
-- Relationship:
-- - customer 1 - N kyc_profile
-- - loan_application 0/1 - 1 kyc_profile
--
-- Design notes:
-- - customer_id is NOT NULL because KYC verifies a specific customer.
-- - loan_application_id is NULLABLE because KYC can be created before the loan application.
-- - UNIQUE(loan_application_id) ensures one loan application can have at most one KYC profile.
-- - PostgreSQL allows multiple NULL values in a UNIQUE constraint, so multiple pre-application
--   KYC profiles are allowed before they are linked to loan applications.
-- - status and overall_score are intentionally not stored. Backend can derive them from
--   the raw check result fields.

CREATE TABLE kyc_profile (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    customer_id UUID NOT NULL
        REFERENCES customer(id),

    loan_application_id UUID
        REFERENCES loan_application(id) ON DELETE SET NULL,

    -- TRUE  = customer is in blacklist
    -- FALSE = customer is not in blacklist
    -- NULL  = not checked yet
    blacklist_check_result BOOLEAN,

    -- TRUE  = OTP verification passed
    -- FALSE = OTP verification failed
    -- NULL  = not verified yet
    phone_otp_verification_result BOOLEAN,

    -- Face eKYC scores from 0 to 100.
    -- Higher score means better trust/confidence.
    face_match_score NUMERIC(5, 2),
    liveness_detection_score NUMERIC(5, 2),
    face_authenticity_score NUMERIC(5, 2),

    checked_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    note TEXT,

    CONSTRAINT uq_kyc_profile_loan_application
        UNIQUE (loan_application_id),

    CONSTRAINT chk_kyc_face_match_score
        CHECK (
            face_match_score IS NULL
            OR (face_match_score >= 0 AND face_match_score <= 100)
        ),

    CONSTRAINT chk_kyc_liveness_detection_score
        CHECK (
            liveness_detection_score IS NULL
            OR (liveness_detection_score >= 0 AND liveness_detection_score <= 100)
        ),

    CONSTRAINT chk_kyc_face_authenticity_score
        CHECK (
            face_authenticity_score IS NULL
            OR (face_authenticity_score >= 0 AND face_authenticity_score <= 100)
        )
);

CREATE INDEX idx_kyc_profile_customer_id
ON kyc_profile(customer_id);

CREATE INDEX idx_kyc_profile_loan_application_id
ON kyc_profile(loan_application_id);
