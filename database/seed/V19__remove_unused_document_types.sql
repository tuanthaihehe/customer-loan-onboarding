-- Customer Loan Onboarding - Remove Unused Document Types
-- PostgreSQL dialect
-- Version: V19 seed
--
-- Scope:
-- - Remove document_type records that are no longer supported.
-- - Remove uploaded document rows that depend on those document types first
--   because loan_application_document.document_type_id is a foreign key.
--
-- This file is idempotent and can be run multiple times safely.
-- It must be run after V10__seed_document_type.sql and V17__add_additional_document_types.sql.

DELETE FROM loan_application_document lad
USING document_type dt
WHERE lad.document_type_id = dt.id
  AND dt.code IN (
      'SIGNED_CUSTOMER_CONTRACT',
      'REFERENCE_VERIFICATION_FORM',
      'OTHER_DOCUMENT'
  );

DELETE FROM document_type
WHERE code IN (
    'SIGNED_CUSTOMER_CONTRACT',
    'REFERENCE_VERIFICATION_FORM',
    'OTHER_DOCUMENT'
);
