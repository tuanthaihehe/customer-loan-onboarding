-- Allow multiple uploaded files under the same document type for one loan application.

ALTER TABLE loan_application_document
DROP CONSTRAINT IF EXISTS uq_loan_application_document_type;

CREATE INDEX IF NOT EXISTS idx_loan_application_document_application_type
ON loan_application_document(loan_application_id, document_type_id);
