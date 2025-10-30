-- Add support for storing contract duration while remaining compatible with older MySQL versions
SET @tsuki_job_duration_months_exists := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'tsuki_job'
      AND COLUMN_NAME = 'duration_months'
);

SET @add_duration_months_sql := IF(
    @tsuki_job_duration_months_exists = 0,
    'ALTER TABLE tsuki_job ADD COLUMN duration_months INT NULL;',
    'DO 0'
);

PREPARE add_duration_months_stmt FROM @add_duration_months_sql;
EXECUTE add_duration_months_stmt;
DEALLOCATE PREPARE add_duration_months_stmt;
