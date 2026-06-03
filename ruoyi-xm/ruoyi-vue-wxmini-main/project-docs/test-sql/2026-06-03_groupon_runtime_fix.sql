USE `ruoyi-cs`;

DROP PROCEDURE IF EXISTS add_groupon_activity_column_if_missing;
DROP PROCEDURE IF EXISTS add_groupon_activity_item_column_if_missing;
DROP PROCEDURE IF EXISTS add_groupon_activity_item_index_if_missing;

DELIMITER //

CREATE PROCEDURE add_groupon_activity_column_if_missing(IN p_column_name VARCHAR(64), IN p_ddl TEXT)
BEGIN
  IF NOT EXISTS (
    SELECT 1
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'groupon_activity'
      AND column_name = p_column_name
  ) THEN
    SET @sql = CONCAT('ALTER TABLE `groupon_activity` ADD COLUMN ', p_ddl);
    PREPARE stmt FROM @sql;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
  END IF;
END//

CREATE PROCEDURE add_groupon_activity_item_column_if_missing(IN p_column_name VARCHAR(64), IN p_ddl TEXT)
BEGIN
  IF NOT EXISTS (
    SELECT 1
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'groupon_activity_item'
      AND column_name = p_column_name
  ) THEN
    SET @sql = CONCAT('ALTER TABLE `groupon_activity_item` ADD COLUMN ', p_ddl);
    PREPARE stmt FROM @sql;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
  END IF;
END//

CREATE PROCEDURE add_groupon_activity_item_index_if_missing(IN p_index_name VARCHAR(64), IN p_ddl TEXT)
BEGIN
  IF NOT EXISTS (
    SELECT 1
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'groupon_activity_item'
      AND index_name = p_index_name
  ) THEN
    SET @sql = CONCAT('CREATE INDEX `', p_index_name, '` ON `groupon_activity_item` ', p_ddl);
    PREPARE stmt FROM @sql;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
  END IF;
END//

DELIMITER ;

CALL add_groupon_activity_column_if_missing('poster_image', '`poster_image` VARCHAR(255) DEFAULT '''' COMMENT ''活动海报图'' AFTER `cover_image`');
CALL add_groupon_activity_column_if_missing('detail_images', '`detail_images` TEXT DEFAULT NULL COMMENT ''活动详情图JSON数组'' AFTER `poster_image`');
CALL add_groupon_activity_column_if_missing('sort', '`sort` INT DEFAULT 0 COMMENT ''排序'' AFTER `limit_per_user`');
CALL add_groupon_activity_column_if_missing('source_type', '`source_type` VARCHAR(20) DEFAULT ''ADMIN'' COMMENT ''创建来源：ADMIN总后台/MERCHANT商家端'' AFTER `sort`');

CALL add_groupon_activity_item_column_if_missing('dish_groups', '`dish_groups` LONGTEXT DEFAULT NULL COMMENT ''菜品组JSON'' AFTER `store_ids`');
CALL add_groupon_activity_item_column_if_missing('dish_total_price', '`dish_total_price` BIGINT DEFAULT 0 COMMENT ''菜品总价，单位分'' AFTER `dish_groups`');
CALL add_groupon_activity_item_column_if_missing('direct_total_price', '`direct_total_price` TINYINT DEFAULT 0 COMMENT ''是否直接设置菜品总价：0否 1是'' AFTER `dish_total_price`');
CALL add_groupon_activity_item_column_if_missing('dish_count', '`dish_count` INT DEFAULT 0 COMMENT ''菜品数量统计'' AFTER `direct_total_price`');
CALL add_groupon_activity_item_column_if_missing('available_dish_count', '`available_dish_count` INT DEFAULT 0 COMMENT ''实际可享用菜品数量'' AFTER `dish_count`');
CALL add_groupon_activity_item_index_if_missing('idx_distributor_id', '(`distributor_id`)');

DROP PROCEDURE IF EXISTS add_groupon_activity_column_if_missing;
DROP PROCEDURE IF EXISTS add_groupon_activity_item_column_if_missing;
DROP PROCEDURE IF EXISTS add_groupon_activity_item_index_if_missing;
