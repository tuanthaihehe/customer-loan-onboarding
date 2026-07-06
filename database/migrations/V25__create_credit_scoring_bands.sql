-- Credit Scoring Band Tables
-- PostgreSQL dialect
-- Replace VXX in the filename with the next Flyway migration version in your repo.
-- Design: 4 catalog tables for a simple scoring model:
--   1) income_score_band
--   2) age_score_band
--   3) dependent_score_band
--   4) overall_score_grade_band

CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE IF NOT EXISTS income_score_band (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    rule_set_code VARCHAR(50) NOT NULL DEFAULT 'BASIC_SCORING_V1',

    min_income_amount NUMERIC(18, 2) NOT NULL,
    max_income_amount NUMERIC(18, 2),

    score_value NUMERIC(10, 2) NOT NULL,
    weight NUMERIC(5, 4) NOT NULL DEFAULT 0.3000,

    display_label VARCHAR(255),
    priority INT NOT NULL DEFAULT 0,

    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    effective_from DATE NOT NULL DEFAULT CURRENT_DATE,
    effective_to DATE,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_income_score_band_range
        CHECK (max_income_amount IS NULL OR max_income_amount > min_income_amount),
    CONSTRAINT chk_income_score_band_score
        CHECK (score_value >= 0 AND score_value <= 100),
    CONSTRAINT chk_income_score_band_weight
        CHECK (weight >= 0 AND weight <= 1),
    CONSTRAINT chk_income_score_band_effective_range
        CHECK (effective_to IS NULL OR effective_to >= effective_from)
);

CREATE TABLE IF NOT EXISTS age_score_band (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    rule_set_code VARCHAR(50) NOT NULL DEFAULT 'BASIC_SCORING_V1',

    min_age INT NOT NULL,
    max_age INT,

    score_value NUMERIC(10, 2) NOT NULL,
    weight NUMERIC(5, 4) NOT NULL DEFAULT 0.4000,

    display_label VARCHAR(255),
    priority INT NOT NULL DEFAULT 0,

    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    effective_from DATE NOT NULL DEFAULT CURRENT_DATE,
    effective_to DATE,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_age_score_band_range
        CHECK (max_age IS NULL OR max_age > min_age),
    CONSTRAINT chk_age_score_band_score
        CHECK (score_value >= 0 AND score_value <= 100),
    CONSTRAINT chk_age_score_band_weight
        CHECK (weight >= 0 AND weight <= 1),
    CONSTRAINT chk_age_score_band_effective_range
        CHECK (effective_to IS NULL OR effective_to >= effective_from)
);

CREATE TABLE IF NOT EXISTS dependent_score_band (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    rule_set_code VARCHAR(50) NOT NULL DEFAULT 'BASIC_SCORING_V1',

    min_dependent_count INT NOT NULL,
    max_dependent_count INT,

    score_value NUMERIC(10, 2) NOT NULL,
    weight NUMERIC(5, 4) NOT NULL DEFAULT 0.3000,

    display_label VARCHAR(255),
    priority INT NOT NULL DEFAULT 0,

    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    effective_from DATE NOT NULL DEFAULT CURRENT_DATE,
    effective_to DATE,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_dependent_score_band_range
        CHECK (max_dependent_count IS NULL OR max_dependent_count > min_dependent_count),
    CONSTRAINT chk_dependent_score_band_score
        CHECK (score_value >= 0 AND score_value <= 100),
    CONSTRAINT chk_dependent_score_band_weight
        CHECK (weight >= 0 AND weight <= 1),
    CONSTRAINT chk_dependent_score_band_effective_range
        CHECK (effective_to IS NULL OR effective_to >= effective_from)
);

CREATE TABLE IF NOT EXISTS overall_score_grade_band (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    rule_set_code VARCHAR(50) NOT NULL DEFAULT 'BASIC_SCORING_V1',

    grade_code VARCHAR(10) NOT NULL,

    min_score NUMERIC(10, 2) NOT NULL,
    max_score NUMERIC(10, 2),

    display_label VARCHAR(255),
    priority INT NOT NULL DEFAULT 0,

    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    effective_from DATE NOT NULL DEFAULT CURRENT_DATE,
    effective_to DATE,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_overall_score_grade_band_range
        CHECK (max_score IS NULL OR max_score > min_score),
    CONSTRAINT chk_overall_score_grade_band_effective_range
        CHECK (effective_to IS NULL OR effective_to >= effective_from)
);

CREATE INDEX IF NOT EXISTS idx_income_score_band_lookup
    ON income_score_band (rule_set_code, is_active, min_income_amount, max_income_amount);

CREATE INDEX IF NOT EXISTS idx_age_score_band_lookup
    ON age_score_band (rule_set_code, is_active, min_age, max_age);

CREATE INDEX IF NOT EXISTS idx_dependent_score_band_lookup
    ON dependent_score_band (rule_set_code, is_active, min_dependent_count, max_dependent_count);

CREATE INDEX IF NOT EXISTS idx_overall_score_grade_band_lookup
    ON overall_score_grade_band (rule_set_code, is_active, min_score, max_score);

COMMENT ON TABLE income_score_band IS 'Catalog table for mapping monthly income amount to income score.';
COMMENT ON TABLE age_score_band IS 'Catalog table for mapping customer age to age score.';
COMMENT ON TABLE dependent_score_band IS 'Catalog table for mapping number of dependents to dependent score.';
COMMENT ON TABLE overall_score_grade_band IS 'Catalog table for mapping overall score to score grade.';
