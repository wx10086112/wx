-- 分销商佣金结算记录表

CREATE TABLE IF NOT EXISTS `distributor_settlement_record` (
  `id`                        BIGINT       NOT NULL AUTO_INCREMENT,
  `settlement_no`             VARCHAR(64)  NOT NULL COMMENT '结算单号',
  `distributor_id`            BIGINT       NOT NULL COMMENT '分销商ID',
  `merchant_id`               BIGINT       DEFAULT NULL COMMENT '关联商家ID',
  `order_no`                  VARCHAR(64)  DEFAULT NULL COMMENT '关联订单号',
  `amount`                    DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '佣金金额，单位元',
  `rate`                      DECIMAL(5,2)  DEFAULT NULL COMMENT '佣金比例',
  `status`                    VARCHAR(32)  NOT NULL DEFAULT 'WAITING_SETTLEMENT' COMMENT 'WAITING_SETTLEMENT/TRANSFERRING/ARRIVED/FAILED/CANCELLED/REVERSED',
  `settlement_period_start`   DATE         DEFAULT NULL COMMENT '结算周期开始',
  `settlement_period_end`     DATE         DEFAULT NULL COMMENT '结算周期结束',
  `expected_transfer_time`    DATETIME     DEFAULT NULL COMMENT '预计打款时间',
  `transfer_time`             DATETIME     DEFAULT NULL COMMENT '发起打款时间',
  `arrive_time`               DATETIME     DEFAULT NULL COMMENT '到账时间',
  `fail_reason`               VARCHAR(500) DEFAULT NULL COMMENT '失败原因',
  `reverse_record_id`         BIGINT       DEFAULT NULL COMMENT '逆向记录ID',
  `del_flag`                  CHAR(1)      DEFAULT '0' COMMENT '删除标志（0存在 2删除）',
  `create_time`               DATETIME     DEFAULT CURRENT_TIMESTAMP,
  `update_time`               DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_settlement_no` (`settlement_no`),
  KEY `idx_distributor_id` (`distributor_id`),
  KEY `idx_merchant_id` (`merchant_id`),
  KEY `idx_order_no` (`order_no`),
  KEY `idx_status` (`status`),
  KEY `idx_expected_transfer_time` (`expected_transfer_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分销商佣金结算记录表';
