-- 独立预约服务表：前端接口保持 /wxmini/groupon/list 不变，后端优先从本表返回预约服务。
CREATE TABLE IF NOT EXISTS `mall_booking_service` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '预约服务ID',
  `merchant_id` BIGINT NOT NULL COMMENT '商家ID',
  `product_id` BIGINT DEFAULT NULL COMMENT '关联商品ID，兼容现有前端productId',
  `service_name` VARCHAR(120) NOT NULL COMMENT '服务名称',
  `service_image` VARCHAR(500) DEFAULT NULL COMMENT '服务图片',
  `service_price` DECIMAL(10,2) DEFAULT 0.00 COMMENT '参考价格',
  `description` VARCHAR(500) DEFAULT NULL COMMENT '服务描述',
  `stock` INT DEFAULT 999999 COMMENT '可预约余量',
  `status` TINYINT DEFAULT 1 COMMENT '状态 1启用 0停用',
  `sort` INT DEFAULT 0 COMMENT '排序',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `del_flag` CHAR(1) DEFAULT '0' COMMENT '删除标志 0存在 2删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_booking_service_product` (`product_id`),
  KEY `idx_booking_service_merchant_status` (`merchant_id`, `status`, `del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预约服务表';

SET @has_booking_service_id := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'mall_booking'
    AND COLUMN_NAME = 'booking_service_id'
);
SET @ddl := IF(
  @has_booking_service_id = 0,
  'ALTER TABLE mall_booking ADD COLUMN booking_service_id BIGINT DEFAULT NULL COMMENT ''预约服务ID'' AFTER user_id',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @has_booking_service_idx := (
  SELECT COUNT(*)
  FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'mall_booking'
    AND INDEX_NAME = 'idx_booking_service_id'
);
SET @ddl := IF(
  @has_booking_service_idx = 0,
  'ALTER TABLE mall_booking ADD INDEX idx_booking_service_id (booking_service_id)',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 用现有上架商品初始化预约服务，名称会去掉审核敏感的团购语义。
INSERT INTO mall_booking_service (
  merchant_id,
  product_id,
  service_name,
  service_image,
  service_price,
  description,
  stock,
  status,
  sort,
  create_time,
  update_time,
  del_flag
)
SELECT
  p.merchant_id,
  p.id,
  TRIM(REPLACE(REPLACE(p.name, '团购', ''), '套餐', '服务')) AS service_name,
  p.cover_image,
  p.price,
  COALESCE(NULLIF(TRIM(REPLACE(REPLACE(p.description, '团购', ''), '套餐', '服务')), ''), '选择到店时间，提交后等待门店确认') AS description,
  COALESCE(NULLIF(p.stock, 0), 999999),
  CASE WHEN p.status = 1 THEN 1 ELSE 0 END,
  COALESCE(p.sort, 0),
  NOW(),
  NOW(),
  '0'
FROM product p
WHERE p.del_flag = '0'
  AND p.status = 1
ON DUPLICATE KEY UPDATE
  merchant_id = VALUES(merchant_id),
  service_name = VALUES(service_name),
  service_image = VALUES(service_image),
  service_price = VALUES(service_price),
  description = VALUES(description),
  stock = VALUES(stock),
  status = VALUES(status),
  sort = VALUES(sort),
  update_time = NOW(),
  del_flag = '0';

-- 回填历史预约记录的服务关联；只按 product_id 能唯一命中的数据回填，不覆盖已有 booking_service_id。
UPDATE mall_booking b
JOIN mall_booking_service s
  ON s.product_id = b.product_id
 AND s.del_flag = '0'
SET b.booking_service_id = s.id
WHERE b.booking_service_id IS NULL
  AND b.product_id IS NOT NULL
  AND b.del_flag = '0';
