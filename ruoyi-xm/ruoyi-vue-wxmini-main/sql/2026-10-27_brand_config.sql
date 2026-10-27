-- ============================================================
-- 小程序品牌配置（可在后台“系统管理-参数设置”修改，无需改代码）
-- 键名规则：mall.brand.xxx.<merchantId>，商家级优先，其次全局 mall.brand.xxx
-- ============================================================
USE `ruoyi-cs`;

INSERT INTO sys_config (config_name, config_key, config_value, config_type, create_by, create_time, update_time, remark)
SELECT '小程序品牌副标题(裕推堂)', 'mall.brand.slogan.5', '生活美容 · 六店通用', 'N', 'admin', NOW(), NOW(),
       '登录页/品牌副标题，改这里即可，无需重新发版'
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE config_key = 'mall.brand.slogan.5');

INSERT INTO sys_config (config_name, config_key, config_value, config_type, create_by, create_time, update_time, remark)
SELECT '小程序品牌提示语(裕推堂)', 'mall.brand.notice.5', '支持微信支付、到店使用、退款售后与消息通知', 'N', 'admin', NOW(), NOW(),
       '品牌提示语，改这里即可'
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE config_key = 'mall.brand.notice.5');

INSERT INTO sys_config (config_name, config_key, config_value, config_type, create_by, create_time, update_time, remark)
SELECT '小程序主题色(裕推堂)', 'mall.brand.primaryColor.5', '#2F7D5B', 'N', 'admin', NOW(), NOW(),
       '品牌主题色，改这里即可'
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE config_key = 'mall.brand.primaryColor.5');

SELECT config_key, config_value FROM sys_config WHERE config_key LIKE 'mall.brand.%' ORDER BY config_key;
