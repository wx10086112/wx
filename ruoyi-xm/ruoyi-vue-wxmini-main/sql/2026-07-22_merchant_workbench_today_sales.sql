-- Covering index for merchant workbench daily paid-sales aggregation.
-- MySQL 5.7 compatible: repeated execution does not create a duplicate index.
SET @index_exists := (
    SELECT COUNT(*)
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'mall_order'
      AND index_name = 'idx_merchant_paid_today'
);

SET @sql := IF(
    @index_exists = 0,
    'ALTER TABLE mall_order ADD INDEX idx_merchant_paid_today (merchant_id, del_flag, status, pay_time, pay_amount)',
    'SELECT 1'
);

PREPARE merchant_paid_today_index_stmt FROM @sql;
EXECUTE merchant_paid_today_index_stmt;
DEALLOCATE PREPARE merchant_paid_today_index_stmt;
