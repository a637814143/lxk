-- Add support for storing contract duration
ALTER TABLE tsuki_job
    ADD COLUMN IF NOT EXISTS duration_months INT;
