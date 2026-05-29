-- 订单三方分账流水表
-- 核销完成后生成，固化当时的分账比例和金额

CREATE TABLE IF NOT EXISTS `order_profit_ledger` (
  `id`               BIGINT       NOT NULL AUTO_INCREMENT,
  `order_no`         VARCHAR(64)  NOT NULL COMMENT '订单号',
  `merchant_id`      BIGINT       NOT NULL COMMENT '商家ID',
  `distributor_id`   BIGINT       DEFAULT NULL COMMENT '分销商ID',
  `pay_amount`       DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '用户实付金额，单位元',
  `merchant_amount`  DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '商家应得金额，单位元',
  `platform_amount`  DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '平台抽成金额，单位元',
  `distributor_amount` DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '分销商佣金金额，单位元',
  `merchant_rate`    DECIMAL(5,2)  DEFAULT NULL COMMENT '商家比例, 如85.00',
  `platform_rate`    DECIMAL(5,2)  DEFAULT NULL COMMENT '平台比例, 如10.00',
  `distributor_rate` DECIMAL(5,2)  DEFAULT NULL COMMENT '分销商比例, 如5.00',
  `status`           VARCHAR(32)  NOT NULL DEFAULT 'WAITING_SETTLEMENT' COMMENT 'WAITING_SETTLEMENT/SETTLED/REFUND_REVERSED',
  `finish_time`      DATETIME     DEFAULT NULL COMMENT '订单完成时间',
  `del_flag`         CHAR(1)      DEFAULT '0' COMMENT '删除标志（0存在 2删除）',
  `create_time`      DATETIME     DEFAULT CURRENT_TIMESTAMP,
  `update_time`      DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_merchant_id` (`merchant_id`),
  KEY `idx_distributor_id` (`distributor_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单三方分账流水表';
