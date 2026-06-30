-- V10__add_lead_customer_status.sql
--
-- BA/DA updated customer.status semantics:
-- - ACTIVE: customer currently has at least one loan in the system.
-- - INACTIVE: existing/returning customer with no active loan or previously settled loan.
-- - BLACKLIST: blacklisted customer.
-- - LEAD: new potential customer that has not had any loan in the system.

ALTER TABLE customer
DROP CONSTRAINT IF EXISTS chk_customer_status;

ALTER TABLE customer
ADD CONSTRAINT chk_customer_status
CHECK (status IN ('ACTIVE', 'INACTIVE', 'BLACKLIST', 'LEAD'));
