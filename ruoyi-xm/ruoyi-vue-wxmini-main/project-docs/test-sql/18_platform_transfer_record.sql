-- 18_platform_transfer_record.sql
-- 微信商家转账记录表 + 商家/分销商收款账户字段

CREATE TABLE `platform_transfer_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '转账记录ID',
  `transfer_no` VARCHAR(64) NOT NULL COMMENT '平台转账单号',
  `settlement_no` VARCHAR(64) NOT NULL COMMENT '关联结算单号',
  `target_type` VARCHAR(16) NOT NULL COMMENT '收款对象类型：MERCHANT/DISTRIBUTOR',
  `target_id` BIGINT NOT NULL COMMENT '收款对象ID',
  `merchant_id` BIGINT DEFAULT NULL COMMENT '关联商家ID',
  `distributor_id` BIGINT DEFAULT NULL COMMENT '关联分销商ID',
  `order_no` VARCHAR(64) DEFAULT NULL COMMENT '关联订单号',
  `amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '转账金额，单位元',
  `receiver_openid` VARCHAR(128) DEFAULT NULL COMMENT '收款人openid',
  `receiver_name` VARCHAR(128) DEFAULT NULL COMMENT '收款人姓名',
  `receiver_account_type` VARCHAR(32) DEFAULT 'WECHAT_BALANCE' COMMENT 'WECHAT_BALANCE/BANK_CARD',
  `wechat_batch_no` VARCHAR(128) DEFAULT NULL COMMENT '微信批次号',
  `wechat_detail_no` VARCHAR(128) DEFAULT NULL COMMENT '微信明细单号',
  `status` VARCHAR(32) NOT NULL DEFAULT 'WAITING' COMMENT 'WAITING/TRANSFERRING/ARRIVED/FAILED/CANCELLED',
  `fail_reason` VARCHAR(500) DEFAULT NULL COMMENT '失败原因',
  `apply_time` DATETIME DEFAULT NULL COMMENT '申请转账时间',
  `transfer_time` DATETIME DEFAULT NULL COMMENT '发起转账时间',
  `arrive_time` DATETIME DEFAULT NULL COMMENT '到账时间',
  `notify_time` DATETIME DEFAULT NULL COMMENT '微信回调时间',
  `notify_result` TEXT DEFAULT NULL COMMENT '微信回调原始结果摘要',
  `operator_id` VARCHAR(64) DEFAULT NULL COMMENT '操作人ID',
  `remark` VARCHAR(500) DEFAULT NULL,
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

-- 商家增加收款账户字段
ALTER TABLE `merchant`
  ADD COLUMN `receiver_openid` VARCHAR(128) DEFAULT NULL COMMENT '收款微信openid',
  ADD COLUMN `receiver_type` VARCHAR(32) DEFAULT 'WECHAT_BALANCE' COMMENT '收款账户类型';

-- 分销商增加收款账户字段
ALTER TABLE `distributor`
  ADD COLUMN `receiver_openid` VARCHAR(128) DEFAULT NULL COMMENT '收款微信openid',
  ADD COLUMN `receiver_type` VARCHAR(32) DEFAULT 'WECHAT_BALANCE' COMMENT '收款账户类型';
