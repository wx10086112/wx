-- Generated from F:/wx/incoming-sql-20260531/sql/01_all_in_one.sql
-- Compared with production schema dump: F:/wx/incoming-sql-20260531/prod-schema-20260531.sql
-- Safe subset only: missing tables and missing columns. No DROP/DELETE/INSERT/UPDATE.
-- Review before production execution. Backup database first.

-- Missing table: `product_image`
CREATE TABLE IF NOT EXISTS `product_image` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '图片ID',
  `product_id` BIGINT NOT NULL COMMENT '商品ID',
  `merchant_id` BIGINT NOT NULL COMMENT '商家ID',
  `image_type` VARCHAR(20) DEFAULT 'main' COMMENT '图片类型(main/detail/sku)',
  `image_url` VARCHAR(500) NOT NULL COMMENT '图片URL',
  `sort_order` INT DEFAULT 0 COMMENT '排序',
  `sku_value` VARCHAR(100) DEFAULT NULL COMMENT 'SKU值',
  `status` TINYINT DEFAULT 1 COMMENT '状态(0删除 1正常)',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_product_id` (`product_id`),
  KEY `idx_merchant_id` (`merchant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品图片表';

-- Missing table: `platform_transfer_record`
CREATE TABLE IF NOT EXISTS `platform_transfer_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `transfer_no` VARCHAR(64) NOT NULL COMMENT '转账单号',
  `settlement_no` VARCHAR(64) NOT NULL COMMENT '关联结算单号',
  `merchant_id` BIGINT DEFAULT NULL COMMENT '商家ID',
  `distributor_id` BIGINT DEFAULT NULL COMMENT '分销商ID',
  `target_type` VARCHAR(32) NOT NULL COMMENT '目标类型(merchant/distributor)',
  `target_id` BIGINT NOT NULL COMMENT '目标ID',
  `amount` DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '转账金额',
  `status` VARCHAR(32) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/TRANSFERRING/SUCCESS/FAILED',
  `wechat_batch_no` VARCHAR(128) DEFAULT NULL COMMENT '微信转账批次号',
  `wechat_detail_no` VARCHAR(128) DEFAULT NULL COMMENT '微信转账明细单号',
  `apply_time` DATETIME DEFAULT NULL COMMENT '申请转账时间',
  `transfer_time` DATETIME DEFAULT NULL COMMENT '发起转账时间',
  `arrive_time` DATETIME DEFAULT NULL COMMENT '到账时间',
  `notify_time` DATETIME DEFAULT NULL COMMENT '微信回调时间',
  `notify_result` TEXT DEFAULT NULL COMMENT '微信回调原始结果',
  `operator_id` BIGINT DEFAULT NULL COMMENT '操作人ID',
  `fail_reason` VARCHAR(500) DEFAULT NULL COMMENT '失败原因',
  `del_flag` CHAR(1) DEFAULT '0' COMMENT '删除标志（0存在 2删除）',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_transfer_no` (`transfer_no`),
  KEY `idx_settlement_no` (`settlement_no`),
  KEY `idx_target` (`target_type`, `target_id`),
  KEY `idx_status` (`status`),
  KEY `idx_wechat_batch_no` (`wechat_batch_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='平台微信转账记录表';

-- Missing table: `sys_user_biz_scope`
CREATE TABLE IF NOT EXISTS `sys_user_biz_scope` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `account_type` VARCHAR(32) DEFAULT 'PLATFORM' COMMENT '账号类型',
  `distributor_id` BIGINT DEFAULT NULL COMMENT '分销商ID',
  `merchant_id` BIGINT DEFAULT NULL COMMENT '商家ID',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户业务范围表';

-- Missing columns for `merchant`
ALTER TABLE `merchant` ADD COLUMN `c_mini_app_id` VARCHAR(64) DEFAULT NULL COMMENT 'C端小程序AppID' AFTER `store_count`;
ALTER TABLE `merchant` ADD COLUMN `c_mini_app_secret` VARCHAR(128) DEFAULT NULL COMMENT 'C端小程序Secret' AFTER `c_mini_app_id`;
ALTER TABLE `merchant` ADD COLUMN `m_mini_app_id` VARCHAR(64) DEFAULT NULL COMMENT '商家端小程序AppID' AFTER `c_mini_app_secret`;
ALTER TABLE `merchant` ADD COLUMN `m_mini_app_secret` VARCHAR(128) DEFAULT NULL COMMENT '商家端小程序Secret' AFTER `m_mini_app_id`;
ALTER TABLE `merchant` ADD COLUMN `wx_pay_mch_id` VARCHAR(64) DEFAULT NULL COMMENT '微信支付商户号' AFTER `m_mini_app_secret`;
ALTER TABLE `merchant` ADD COLUMN `wx_pay_api_key` VARCHAR(256) DEFAULT NULL COMMENT '微信支付API密钥' AFTER `wx_pay_mch_id`;
ALTER TABLE `merchant` ADD COLUMN `receiver_type` VARCHAR(32) DEFAULT NULL COMMENT '收款方类型(OPENID/BANK)' AFTER `wx_pay_api_key`;
ALTER TABLE `merchant` ADD COLUMN `receiver_openid` VARCHAR(128) DEFAULT NULL COMMENT '微信收款openId' AFTER `receiver_type`;

-- Missing columns for `groupon_activity`
ALTER TABLE `groupon_activity` ADD COLUMN `source_type` VARCHAR(20) DEFAULT 'ADMIN' COMMENT '来源(ADMIN后台/MERCHANT商家)' AFTER `description`;

-- Missing columns for `product`
ALTER TABLE `product` ADD COLUMN `main_image` VARCHAR(255) DEFAULT NULL COMMENT '主图' AFTER `cover_image`;
ALTER TABLE `product` ADD COLUMN `verify_notice` VARCHAR(255) DEFAULT NULL COMMENT '核销提示' AFTER `valid_days`;

-- Missing columns for `coupon`
ALTER TABLE `coupon` ADD COLUMN `remain_count` INT DEFAULT 0 COMMENT '剩余数量' AFTER `total_count`;
ALTER TABLE `coupon` ADD COLUMN `remark` VARCHAR(500) DEFAULT '' COMMENT '备注' AFTER `update_time`;

-- Missing columns for `user_address`
ALTER TABLE `user_address` ADD COLUMN `address` VARCHAR(255) NOT NULL COMMENT '详细地址' AFTER `phone`;

-- Missing columns for `mall_user`
ALTER TABLE `mall_user` ADD COLUMN `union_id` VARCHAR(64) DEFAULT NULL COMMENT '微信unionId' AFTER `open_id`;
ALTER TABLE `mall_user` ADD COLUMN `nick_name` VARCHAR(100) DEFAULT '' COMMENT '昵称' AFTER `union_id`;
ALTER TABLE `mall_user` ADD COLUMN `avatar_url` VARCHAR(255) DEFAULT '' COMMENT '头像' AFTER `nick_name`;
ALTER TABLE `mall_user` ADD COLUMN `last_login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间' AFTER `total_amount`;

-- Missing columns for `distributor`
ALTER TABLE `distributor` ADD COLUMN `receiver_openid` VARCHAR(128) DEFAULT NULL COMMENT '微信收款openId' AFTER `region_name`;
ALTER TABLE `distributor` ADD COLUMN `receiver_type` VARCHAR(32) DEFAULT 'OPENID' COMMENT '收款方类型(OPENID/BANK)' AFTER `receiver_openid`;

-- Missing columns for `mall_order`
ALTER TABLE `mall_order` ADD COLUMN `write_off_status` INT DEFAULT 0 COMMENT '核销状态(0未核销 1已核销)' AFTER `write_off_code`;
ALTER TABLE `mall_order` ADD COLUMN `write_off_time` DATETIME DEFAULT NULL COMMENT '核销时间' AFTER `write_off_status`;
ALTER TABLE `mall_order` ADD COLUMN `write_off_user_id` BIGINT DEFAULT NULL COMMENT '核销操作人ID' AFTER `write_off_time`;
ALTER TABLE `mall_order` ADD COLUMN `valid_days` INT DEFAULT NULL COMMENT '商品有效天数' AFTER `write_off_user_id`;

-- Missing columns for `transaction_record`
ALTER TABLE `transaction_record` ADD COLUMN `remark` VARCHAR(500) DEFAULT '' COMMENT '备注' AFTER `order_no`;

-- Missing columns for `merchant_bill`
ALTER TABLE `merchant_bill` ADD COLUMN `bill_period` VARCHAR(10) NOT NULL COMMENT '账单周期(如2026-05)' AFTER `merchant_id`;
ALTER TABLE `merchant_bill` ADD COLUMN `remark` VARCHAR(500) DEFAULT '' COMMENT '备注' AFTER `update_time`;

-- Missing columns for `cart`
ALTER TABLE `cart` ADD COLUMN `product_name` VARCHAR(200) DEFAULT '' COMMENT '商品名称' AFTER `product_id`;
ALTER TABLE `cart` ADD COLUMN `product_image` VARCHAR(255) DEFAULT '' COMMENT '商品图片' AFTER `product_name`;
ALTER TABLE `cart` ADD COLUMN `price` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '单价' AFTER `product_image`;

-- Missing columns for `banner`
ALTER TABLE `banner` ADD COLUMN `merchant_id` BIGINT DEFAULT NULL COMMENT '商家ID(NULL为平台级)' AFTER `id`;
ALTER TABLE `banner` ADD COLUMN `image_url` VARCHAR(255) NOT NULL COMMENT '图片地址' AFTER `title`;

-- Missing columns for `operation_log`
ALTER TABLE `operation_log` ADD COLUMN `merchant_id` BIGINT DEFAULT NULL COMMENT '商家ID' AFTER `id`;
ALTER TABLE `operation_log` ADD COLUMN `operator_id` BIGINT DEFAULT NULL COMMENT '操作人ID' AFTER `merchant_id`;
ALTER TABLE `operation_log` ADD COLUMN `target` VARCHAR(100) DEFAULT '' COMMENT '操作对象' AFTER `action`;
ALTER TABLE `operation_log` ADD COLUMN `detail` VARCHAR(500) DEFAULT '' COMMENT '详情' AFTER `target`;
ALTER TABLE `operation_log` ADD COLUMN `ip_address` VARCHAR(50) DEFAULT '' COMMENT 'IP地址' AFTER `detail`;
ALTER TABLE `operation_log` ADD COLUMN `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间' AFTER `ip_address`;

-- Missing columns for `write_off_record`
ALTER TABLE `write_off_record` ADD COLUMN `del_flag` char(1) DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）' AFTER `create_time`;

-- Missing columns for `payment_record`
ALTER TABLE `payment_record` ADD COLUMN `pay_channel` VARCHAR(20) DEFAULT 'WECHAT' COMMENT '支付渠道' AFTER `pay_type`;
ALTER TABLE `payment_record` ADD COLUMN `pay_mch_id` VARCHAR(64) DEFAULT NULL COMMENT '微信商户号' AFTER `pay_channel`;
ALTER TABLE `payment_record` ADD COLUMN `notify_raw` TEXT DEFAULT NULL COMMENT '微信回调原始报文' AFTER `pay_time`;

-- Missing columns for `refund_record`
ALTER TABLE `refund_record` ADD COLUMN `apply_time` DATETIME DEFAULT NULL COMMENT '申请时间' AFTER `status`;
ALTER TABLE `refund_record` ADD COLUMN `remark` VARCHAR(500) DEFAULT '' COMMENT '备注' AFTER `refund_time`;
