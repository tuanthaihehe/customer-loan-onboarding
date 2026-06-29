-- Customer Loan Onboarding - Initial Schema
-- PostgreSQL dialect
--
-- Scope:
-- - Store basic customer information for lookup.
-- - Store basic vehicle asset information owned by customers.
-- - Store loan applications and manage only loan application lifecycle.
--
-- Design notes:
-- - id is the technical primary key used for foreign keys.
-- - *_code fields are business codes used for lookup/display, not FK references.
-- - customer.status and asset.status are simple enum-like values enforced by CHECK constraints.
-- - loan_application lifecycle is managed by state, transition and history tables.
-- - loan_application.asset_id is nullable because a draft application can be created before an asset is attached.

CREATE EXTENSION IF NOT EXISTS pgcrypto;
CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- =========================================================
-- 1. Loan application lifecycle reference tables
-- =========================================================

CREATE TABLE loan_application_state (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    is_initial BOOLEAN NOT NULL DEFAULT FALSE,
    is_terminal BOOLEAN NOT NULL DEFAULT FALSE,
    sort_order INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE loan_application_state_transition (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    from_state_id UUID NOT NULL REFERENCES loan_application_state(id),
    to_state_id UUID NOT NULL REFERENCES loan_application_state(id),
    action_code VARCHAR(50) NOT NULL,
    action_name VARCHAR(100) NOT NULL,
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_loan_application_state_transition
        UNIQUE (from_state_id, to_state_id, action_code),

    CONSTRAINT chk_loan_application_state_transition_no_self_loop
        CHECK (from_state_id <> to_state_id)
);

-- =========================================================
-- 2. Customer
-- =========================================================

CREATE TABLE customer (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    customer_code VARCHAR(50) NOT NULL UNIQUE,
    full_name VARCHAR(255) NOT NULL,
    phone_number VARCHAR(20) NOT NULL UNIQUE,
    identity_number VARCHAR(20) NOT NULL UNIQUE,
    date_of_birth DATE,

    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,

    CONSTRAINT chk_customer_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'RESTRICTED'))
);

-- =========================================================
-- 3. Asset
-- =========================================================

CREATE TABLE asset (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    asset_code VARCHAR(50) NOT NULL UNIQUE,
    customer_id UUID NOT NULL REFERENCES customer(id),

    license_plate VARCHAR(20) NOT NULL UNIQUE,
    vehicle_brand VARCHAR(100) NOT NULL,
    vehicle_model VARCHAR(100) NOT NULL,
    vehicle_version VARCHAR(100),
    manufacture_year INTEGER,

    status VARCHAR(30) NOT NULL DEFAULT 'AVAILABLE',

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,

    CONSTRAINT chk_asset_status
        CHECK (status IN ('AVAILABLE', 'PLEDGED', 'RELEASED')),

    CONSTRAINT chk_asset_manufacture_year
        CHECK (manufacture_year IS NULL OR manufacture_year BETWEEN 1980 AND 2100)
);

-- =========================================================
-- 4. Loan application
-- =========================================================

CREATE TABLE loan_application (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    loan_application_code VARCHAR(50) NOT NULL UNIQUE,

    customer_id UUID NOT NULL REFERENCES customer(id),

    -- Nullable by design:
    -- A draft application can be created before an asset is selected/attached.
    asset_id UUID REFERENCES asset(id),

    current_state_id UUID NOT NULL REFERENCES loan_application_state(id),

    requested_amount NUMERIC(18, 2) NOT NULL,
    loan_purpose TEXT,

    submitted_at TIMESTAMP,
    closed_at TIMESTAMP,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,

    CONSTRAINT chk_loan_application_requested_amount
        CHECK (requested_amount > 0)
);

-- One asset cannot be used by more than one open loan application at the same time.
-- asset_id is nullable, so the index only applies after an asset is attached.
CREATE UNIQUE INDEX uq_active_loan_application_asset
ON loan_application(asset_id)
WHERE asset_id IS NOT NULL
  AND closed_at IS NULL
  AND deleted_at IS NULL;

-- =========================================================
-- 5. Loan application lifecycle history
-- =========================================================

CREATE TABLE loan_application_state_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    loan_application_id UUID NOT NULL REFERENCES loan_application(id),

    from_state_id UUID REFERENCES loan_application_state(id),
    to_state_id UUID NOT NULL REFERENCES loan_application_state(id),

    action_code VARCHAR(50) NOT NULL,
    changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    changed_by VARCHAR(100),
    note TEXT,

    CONSTRAINT chk_loan_application_state_history_no_self_loop
        CHECK (from_state_id IS NULL OR from_state_id <> to_state_id)
);

-- =========================================================
-- 6. Lookup and query indexes
-- =========================================================

CREATE INDEX idx_customer_full_name_trgm
ON customer USING gin (full_name gin_trgm_ops);

CREATE INDEX idx_customer_date_of_birth
ON customer(date_of_birth);

CREATE INDEX idx_customer_status
ON customer(status);

CREATE INDEX idx_asset_customer_id
ON asset(customer_id);

CREATE INDEX idx_asset_status
ON asset(status);

CREATE INDEX idx_loan_application_customer_id
ON loan_application(customer_id);

CREATE INDEX idx_loan_application_current_state_id
ON loan_application(current_state_id);

CREATE INDEX idx_loan_application_state_history_application_id
ON loan_application_state_history(loan_application_id);

CREATE INDEX idx_loan_application_state_history_changed_at
ON loan_application_state_history(changed_at);
