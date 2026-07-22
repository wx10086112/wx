-- Index for the live merchant-detail product count query.
-- MySQL 5.7 compatible: repeated execution does not create a duplicate index.
SET @index_exists := (
    SELECT COUNT(*)
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'product'
      AND index_name = 'idx_merchant_del_flag'
);

SET @sql := IF(
    @index_exists = 0,
    'ALTER TABLE product ADD INDEX idx_merchant_del_flag (merchant_id, del_flag)',
    'SELECT 1'
);

PREPARE merchant_detail_product_index_stmt FROM @sql;
EXECUTE merchant_detail_product_index_stmt;
DEALLOCATE PREPARE merchant_detail_product_index_stmt;
