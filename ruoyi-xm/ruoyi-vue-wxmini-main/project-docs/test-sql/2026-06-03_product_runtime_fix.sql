USE `ruoyi-cs`;

DROP PROCEDURE IF EXISTS add_product_column_if_missing;
DROP PROCEDURE IF EXISTS add_product_index_if_missing;

DELIMITER //

CREATE PROCEDURE add_product_column_if_missing(IN p_column_name VARCHAR(64), IN p_ddl TEXT)
BEGIN
  IF NOT EXISTS (
    SELECT 1
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'product'
      AND column_name = p_column_name
  ) THEN
    SET @sql = CONCAT('ALTER TABLE `product` ADD COLUMN ', p_ddl);
    PREPARE stmt FROM @sql;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
  END IF;
END//

CREATE PROCEDURE add_product_index_if_missing(IN p_index_name VARCHAR(64), IN p_ddl TEXT)
BEGIN
  IF NOT EXISTS (
    SELECT 1
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'product'
      AND index_name = p_index_name
  ) THEN
    SET @sql = CONCAT('CREATE INDEX `', p_index_name, '` ON `product` ', p_ddl);
    PREPARE stmt FROM @sql;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
  END IF;
END//

DELIMITER ;

CALL add_product_column_if_missing('main_image', '`main_image` VARCHAR(500) DEFAULT NULL COMMENT ''商品主图URL(冗余，快速展示用)'' AFTER `images`');
CALL add_product_column_if_missing('verify_type', '`verify_type` TINYINT DEFAULT 1 COMMENT ''核销方式: 1在线核销 2到店自提'' AFTER `status`');
CALL add_product_index_if_missing('idx_verify_type', '(`verify_type`)');

DROP PROCEDURE IF EXISTS add_product_column_if_missing;
DROP PROCEDURE IF EXISTS add_product_index_if_missing;
