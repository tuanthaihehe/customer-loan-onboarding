-- Customer Loan Onboarding - Add Loan Application Reference Person
-- PostgreSQL dialect
-- Version: V11
--
-- Scope:
-- - Add table to store reference persons declared for a loan application.
-- - relationship_type is implemented as VARCHAR + CHECK enum for simplicity.
--
-- Design notes:
-- - Reference persons belong to a loan application, not to customer.
-- - One loan application can have multiple reference persons.
-- - The same phone number can appear in different loan applications,
--   but should not be duplicated within the same loan application.

CREATE TABLE loan_application_reference_person (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    loan_application_id UUID NOT NULL REFERENCES loan_application(id) ON DELETE CASCADE,

    full_name VARCHAR(255) NOT NULL,
    phone_number VARCHAR(20) NOT NULL,
    address TEXT,

    relationship_type VARCHAR(50) NOT NULL,

    note TEXT,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_loan_application_reference_relationship_type
        CHECK (relationship_type IN (
            'FATHER',
            'MOTHER',
            'SPOUSE',
            'SIBLING',
            'RELATIVE',
            'FRIEND',
            'COLLEAGUE',
            'OTHER'
        )),

    CONSTRAINT uq_reference_phone_per_application
        UNIQUE (loan_application_id, phone_number)
);

CREATE INDEX idx_reference_person_loan_application_id
ON loan_application_reference_person(loan_application_id);

CREATE INDEX idx_reference_person_phone_number
ON loan_application_reference_person(phone_number);

CREATE INDEX idx_reference_person_relationship_type
ON loan_application_reference_person(relationship_type);
