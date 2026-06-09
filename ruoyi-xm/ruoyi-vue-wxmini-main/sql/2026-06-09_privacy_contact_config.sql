-- 2026-06-09 privacy/contact runtime config.
-- Purpose: provide one source of truth for miniapp privacy guide, contact page,
-- and WeChat privacy backend fill-in information.
--
-- After changing these values in SQL, restart the backend or refresh sys_config cache
-- in the admin console so /wxmini/template/config returns the latest values.

SET NAMES utf8mb4;

INSERT INTO sys_config
(config_name, config_key, config_value, config_type, create_by, create_time, remark)
SELECT '商城合规-运营主体名称', 'mall.privacy.operatorName', '', 'N', 'migration', NOW(),
       '微信隐私保护指引中的个人信息处理者/运营主体真实名称'
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE config_key = 'mall.privacy.operatorName');

INSERT INTO sys_config
(config_name, config_key, config_value, config_type, create_by, create_time, remark)
SELECT '商城合规-客服电话', 'mall.privacy.servicePhone', '', 'N', 'migration', NOW(),
       '微信隐私保护指引和小程序联系客服页展示的客服电话'
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE config_key = 'mall.privacy.servicePhone');

INSERT INTO sys_config
(config_name, config_key, config_value, config_type, create_by, create_time, remark)
SELECT '商城合规-联系邮箱', 'mall.privacy.contactEmail', '', 'N', 'migration', NOW(),
       '微信隐私保护指引和小程序联系客服页展示的联系邮箱'
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE config_key = 'mall.privacy.contactEmail');

INSERT INTO sys_config
(config_name, config_key, config_value, config_type, create_by, create_time, remark)
SELECT '商城合规-联系地址', 'mall.privacy.contactAddress', '', 'N', 'migration', NOW(),
       '微信隐私保护指引和小程序联系客服页展示的注册地址或常用联系地址'
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE config_key = 'mall.privacy.contactAddress');

INSERT INTO sys_config
(config_name, config_key, config_value, config_type, create_by, create_time, remark)
SELECT '商城合规-客服时间', 'mall.privacy.businessHoursText', '', 'N', 'migration', NOW(),
       '小程序联系客服页展示的客服服务时间，如工作日 09:00-18:00'
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE config_key = 'mall.privacy.businessHoursText');

INSERT INTO sys_config
(config_name, config_key, config_value, config_type, create_by, create_time, remark)
SELECT '商城合规-个人信息权利请求说明', 'mall.privacy.rightsRequestTips',
       '如你对个人信息处理有查阅、复制、更正、删除、撤回授权、注销或投诉建议等需求，可通过小程序“联系客服”、订单详情页商家联系方式或微信小程序主体公示联系方式提交。',
       'N', 'migration', NOW(), '隐私保护指引联系方式与用户权利请求说明'
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE config_key = 'mall.privacy.rightsRequestTips');
