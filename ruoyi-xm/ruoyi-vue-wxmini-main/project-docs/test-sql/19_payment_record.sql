-- payment_record 支付记录表
-- 用于全链路留痕：发起支付→回调→退款每个环节都有记录
CREATE TABLE IF NOT EXISTS payment_record (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    order_no VARCHAR(64) NOT NULL COMMENT '订单号',
    merchant_id BIGINT DEFAULT NULL COMMENT '商家ID',
    user_id BIGINT DEFAULT NULL COMMENT '用户ID',
    amount DECIMAL(10,2) DEFAULT NULL COMMENT '支付金额（元）',
    pay_type VARCHAR(20) DEFAULT 'JSAPI' COMMENT '支付类型: JSAPI/NATIVE/APP',
    transaction_id VARCHAR(64) DEFAULT NULL COMMENT '微信支付交易号',
    out_trade_no VARCHAR(64) DEFAULT NULL COMMENT '商户订单号',
    pay_status INT DEFAULT 0 COMMENT '支付状态: 0待支付, 1已支付, 2已关闭, 3已退款',
    pay_time DATETIME DEFAULT NULL COMMENT '支付完成时间',
    notify_result TEXT DEFAULT NULL COMMENT '回调原始内容',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    del_flag CHAR(1) DEFAULT '0' COMMENT '删除标志(0存在 2删除)',
    active_order_no VARCHAR(64) GENERATED ALWAYS AS (CASE WHEN del_flag = '0' THEN order_no ELSE NULL END) STORED COMMENT '有效订单号唯一约束辅助列',
    PRIMARY KEY (id),
    UNIQUE KEY uk_payment_record_active_order_no (active_order_no),
    KEY idx_order_no (order_no),
    KEY idx_out_trade_no (out_trade_no),
    KEY idx_merchant_id (merchant_id),
    KEY idx_user_id (user_id),
    KEY idx_pay_status (pay_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付记录表';
