-- 分销商表
CREATE TABLE IF NOT EXISTS `distributor` (
  `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name`            VARCHAR(200) NOT NULL COMMENT '分销商名称',
  `contact`         VARCHAR(100) DEFAULT NULL COMMENT '联系人',
  `phone`           VARCHAR(20)  DEFAULT NULL COMMENT '联系电话',
  `username`        VARCHAR(100) NOT NULL COMMENT '登录账号',
  `password`        VARCHAR(200) NOT NULL COMMENT '登录密码 BCrypt',
  `region_code`     VARCHAR(50)  DEFAULT NULL COMMENT '区域编码',
  `region_name`     VARCHAR(100) DEFAULT NULL COMMENT '区域名称',
  `status`          INT          NOT NULL DEFAULT 1 COMMENT '状态：0禁用 1正常',
  `remark`          VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `create_time`     DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`     DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分销商表';

-- merchant 表增加 distributor_id
ALTER TABLE `merchant` ADD COLUMN `distributor_id` BIGINT DEFAULT NULL COMMENT '所属分销商ID' AFTER `id`;
ALTER TABLE `merchant` ADD KEY `idx_distributor_id` (`distributor_id`);

-- 核心业务表增加 distributor_id（如已存在则跳过）
-- 以下语句如果报错"Duplicate column"可忽略

-- groupon_activity
ALTER TABLE `groupon_activity` ADD COLUMN `distributor_id` BIGINT DEFAULT NULL COMMENT '分销商ID' AFTER `merchant_id`;
ALTER TABLE `groupon_activity` ADD KEY `idx_distributor_id` (`distributor_id`);

-- groupon_activity_item
ALTER TABLE `groupon_activity_item` ADD COLUMN `distributor_id` BIGINT DEFAULT NULL COMMENT '分销商ID' AFTER `merchant_id`;
ALTER TABLE `groupon_activity_item` ADD KEY `idx_distributor_id` (`distributor_id`);
