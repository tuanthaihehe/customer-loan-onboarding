-- Customer Loan Onboarding - Asset Valuation and Fixed Deduction
-- PostgreSQL dialect
-- Version: V6

CREATE TABLE asset_deduction_type (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    code VARCHAR(50) NOT NULL,
    name VARCHAR(100) NOT NULL,
    description TEXT,

    deduction_amount NUMERIC(18, 2) NOT NULL,

    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    sort_order INT NOT NULL DEFAULT 0,

    CONSTRAINT uq_asset_deduction_type_code
        UNIQUE (code),

    CONSTRAINT chk_asset_deduction_type_amount
        CHECK (deduction_amount >= 0)
);

CREATE TABLE asset_valuation (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    asset_id UUID NOT NULL REFERENCES asset(id),

    market_price_amount NUMERIC(18, 2) NOT NULL,
    total_deduction_amount NUMERIC(18, 2) NOT NULL DEFAULT 0,
    final_value_amount NUMERIC(18, 2) NOT NULL,

    currency_code VARCHAR(3) NOT NULL DEFAULT 'VND',

    valuation_source VARCHAR(100),
    valued_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    valued_by VARCHAR(100),
    note TEXT,

    CONSTRAINT chk_asset_valuation_market_price_amount
        CHECK (market_price_amount > 0),

    CONSTRAINT chk_asset_valuation_total_deduction_amount
        CHECK (total_deduction_amount >= 0),

    CONSTRAINT chk_asset_valuation_final_value_amount
        CHECK (final_value_amount >= 0),

    CONSTRAINT chk_asset_valuation_final_not_greater_than_market
        CHECK (final_value_amount <= market_price_amount),

    CONSTRAINT chk_asset_valuation_deduction_not_greater_than_market
        CHECK (total_deduction_amount <= market_price_amount),

    CONSTRAINT chk_asset_valuation_final_matches_deduction
        CHECK (final_value_amount = market_price_amount - total_deduction_amount)
);

CREATE INDEX idx_asset_valuation_asset_id
ON asset_valuation(asset_id);

CREATE INDEX idx_asset_valuation_valued_at
ON asset_valuation(valued_at);

CREATE TABLE asset_valuation_deduction (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    asset_valuation_id UUID NOT NULL REFERENCES asset_valuation(id) ON DELETE CASCADE,
    deduction_type_id UUID NOT NULL REFERENCES asset_deduction_type(id),

    deduction_amount_snapshot NUMERIC(18, 2) NOT NULL,

    note TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_asset_valuation_deduction_amount_snapshot
        CHECK (deduction_amount_snapshot >= 0),

    CONSTRAINT uq_asset_valuation_deduction_type
        UNIQUE (asset_valuation_id, deduction_type_id)
);

CREATE INDEX idx_asset_valuation_deduction_valuation_id
ON asset_valuation_deduction(asset_valuation_id);

CREATE INDEX idx_asset_valuation_deduction_type_id
ON asset_valuation_deduction(deduction_type_id);