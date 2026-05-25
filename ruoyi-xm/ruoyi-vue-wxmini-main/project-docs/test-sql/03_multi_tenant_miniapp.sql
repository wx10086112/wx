-- 2026-05-19 多租户小程序架构：merchant表新增字段
-- 每个商家独立AppID，C端+商家端各一个

ALTER TABLE merchant ADD COLUMN c_mini_app_id VARCHAR(64) DEFAULT NULL COMMENT 'C端小程序AppID';
ALTER TABLE merchant ADD COLUMN c_mini_app_secret VARCHAR(128) DEFAULT NULL COMMENT 'C端小程序Secret';
ALTER TABLE merchant ADD COLUMN m_mini_app_id VARCHAR(64) DEFAULT NULL COMMENT '商家端小程序AppID';
ALTER TABLE merchant ADD COLUMN m_mini_app_secret VARCHAR(128) DEFAULT NULL COMMENT '商家端小程序Secret';
ALTER TABLE merchant ADD COLUMN wx_pay_mch_id VARCHAR(32) DEFAULT NULL COMMENT '微信商户号';
ALTER TABLE merchant ADD COLUMN wx_pay_api_key VARCHAR(128) DEFAULT NULL COMMENT '微信支付API密钥';

-- 唯一索引，按AppID反查商家
CREATE UNIQUE INDEX idx_merchant_c_app_id ON merchant(c_mini_app_id);
CREATE UNIQUE INDEX idx_merchant_m_app_id ON merchant(m_mini_app_id);

-- 示例：为测试商家填入占位AppID（需替换为真实值）
-- UPDATE merchant SET c_mini_app_id = 'wx_c_test_001', c_mini_app_secret = 'secret_c_test_001' WHERE id = 1;
-- UPDATE merchant SET m_mini_app_id = 'wx_m_test_001', m_mini_app_secret = 'secret_m_test_001' WHERE id = 1;
