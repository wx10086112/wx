-- 商家结算记录表
-- 订单核销完成后生成，支撑 T+1 自动打款结算链路

CREATE TABLE IF NOT EXISTS `merchant_settlement_record` (
  `id`                        BIGINT       NOT NULL AUTO_INCREMENT COMMENT '结算记录ID',
  `settlement_no`             VARCHAR(64)  NOT NULL COMMENT '结算单号',
  `merchant_id`               BIGINT       NOT NULL COMMENT '商家ID',
  `distributor_id`            BIGINT       DEFAULT NULL COMMENT '分销商ID',
  `store_id`                  BIGINT       DEFAULT NULL COMMENT '门店ID',
  `order_no`                  VARCHAR(64)  DEFAULT NULL COMMENT '关联订单号',
  `title`                     VARCHAR(255) DEFAULT NULL COMMENT '结算标题/商品名称',
  `order_amount`              DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '订单金额，单位元',
  `merchant_amount`           DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '商家结算金额，单位元',
  `platform_fee_amount`       DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '平台佣金，单位元',
  `status`                    VARCHAR(32)  NOT NULL DEFAULT 'WAITING_T1' COMMENT 'WAITING_T1/TRANSFERRING/ARRIVED/FAILED/CANCELLED/REFUND_PROCESSING/REVERSED',
  `apply_time`                DATETIME     DEFAULT NULL COMMENT '进入结算链路时间',
  `expected_transfer_time`    DATETIME     DEFAULT NULL COMMENT '预计打款时间',
  `transfer_time`             DATETIME     DEFAULT NULL COMMENT '发起打款时间',
  `arrive_time`               DATETIME     DEFAULT NULL COMMENT '到账时间',
  `fail_reason`               VARCHAR(500) DEFAULT NULL COMMENT '失败原因',
  `wechat_batch_no`           VARCHAR(128) DEFAULT NULL COMMENT '微信批次号',
  `wechat_detail_no`          VARCHAR(128) DEFAULT NULL COMMENT '微信明细单号',
  `reverse_record_id`         BIGINT       DEFAULT NULL COMMENT '逆向/负向记录ID',
  `del_flag`                  CHAR(1)      DEFAULT '0' COMMENT '删除标志（0存在 2删除）',
  `create_time`               DATETIME     DEFAULT CURRENT_TIMESTAMP,
  `update_time`               DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_settlement_no` (`settlement_no`),
  KEY `idx_merchant_status` (`merchant_id`, `status`),
  KEY `idx_order_no` (`order_no`),
  KEY `idx_expected_transfer_time` (`expected_transfer_time`),
  KEY `idx_distributor_id` (`distributor_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商家结算记录表';
