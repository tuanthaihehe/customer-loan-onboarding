-- Customer Loan Onboarding - Add Loan Application Document Management
-- PostgreSQL dialect
-- Version: V17
--
-- Scope:
-- - Create document_type catalog.
-- - Create loan_application_document to store uploaded document/image references
--   for each loan application.
--
-- Design notes:
-- - document_type defines what kinds of documents/images the application can have.
-- - loan_application_document stores the actual uploaded file reference for a loan application.
-- - This simplified design intentionally does not store mime_type, file_size_bytes, or status.
-- - One loan application can have at most one file for each document type.

CREATE TABLE document_type (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    code VARCHAR(50) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,

    is_required BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    sort_order INT NOT NULL DEFAULT 0,

    CONSTRAINT uq_document_type_code
        UNIQUE (code)
);

CREATE TABLE loan_application_document (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    loan_application_id UUID NOT NULL
        REFERENCES loan_application(id) ON DELETE CASCADE,

    document_type_id UUID NOT NULL
        REFERENCES document_type(id),

    file_url TEXT NOT NULL,
    file_name VARCHAR(255),

    uploaded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    uploaded_by VARCHAR(100),

    note TEXT,

    CONSTRAINT uq_loan_application_document_type
        UNIQUE (loan_application_id, document_type_id)
);

CREATE INDEX idx_loan_application_document_application_id
ON loan_application_document(loan_application_id);

CREATE INDEX idx_loan_application_document_type_id
ON loan_application_document(document_type_id);
