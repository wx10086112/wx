-- 团购商品明细表
-- 一个团购活动下可以有多个团购商品，每个商品独立维护价格/库存/图片等信息

CREATE TABLE IF NOT EXISTS `groupon_activity_item` (
  `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `merchant_id`     BIGINT       NOT NULL COMMENT '商家ID',
  `groupon_id`      BIGINT       NOT NULL COMMENT '所属团购活动ID',
  `name`            VARCHAR(200) NOT NULL COMMENT '团购商品名称',
  `title`           VARCHAR(200) DEFAULT NULL COMMENT '展示标题',
  `content`         TEXT         DEFAULT NULL COMMENT '套餐内容/服务内容',
  `description`     VARCHAR(500) DEFAULT NULL COMMENT '商品说明',
  `cover_image`     VARCHAR(500) DEFAULT NULL COMMENT '封面图URL',
  `detail_images`   TEXT         DEFAULT NULL COMMENT '详情图JSON数组',
  `original_price`  DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '原价，单位元',
  `groupon_price`   DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '团购价/现价，单位元',
  `discount_rate`   DECIMAL(5,2) DEFAULT NULL COMMENT '折扣，如7.5表示7.5折',
  `stock`           INT          NOT NULL DEFAULT 0 COMMENT '团购库存',
  `sales`           INT          NOT NULL DEFAULT 0 COMMENT '团购销量',
  `limit_per_user`  INT          NOT NULL DEFAULT 0 COMMENT '每人限购，0不限',
  `valid_days`      INT          NOT NULL DEFAULT 30 COMMENT '购买后有效天数',
  `store_ids`       VARCHAR(500) DEFAULT NULL COMMENT '可用门店ID JSON数组',
  `status`          INT          NOT NULL DEFAULT 0 COMMENT '状态：0下架 1上架',
  `sort`            INT          NOT NULL DEFAULT 0 COMMENT '排序值，越大越靠前',
  `del_flag`        char(1)      DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
  `create_time`     DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`     DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_groupon_id` (`groupon_id`),
  KEY `idx_merchant_id` (`merchant_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='团购商品明细表';
