ALTER TABLE tsuki_financial_transaction
    ADD COLUMN IF NOT EXISTS duration_months INT;
