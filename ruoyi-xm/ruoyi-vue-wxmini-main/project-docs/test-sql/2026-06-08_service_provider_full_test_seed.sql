-- 2026-06-08 service-provider full-chain test seed.
-- Scope: merchant miniapp config, WeChat Pay service-provider mode, orders, refunds,
-- settlement, transfer, and backend data-scope accounts.
--
-- IMPORTANT:
-- 1. Run existing schema/hotfix scripts first, especially:
--    - project-docs/test-sql/08_order_alter.sql
--    - project-docs/test-sql/05_product_image.sql
--    - project-docs/test-sql/10_groupon_activity_item.sql
--    - project-docs/test-sql/14_soft_delete_and_amount.sql
--    - project-docs/test-sql/18_platform_transfer_record.sql
--    - project-docs/test-sql/2026-06-01_merchant_payment_share_fields.sql
--    - project-docs/test-sql/2026-06-04_distributor_admin_role_menu.sql
--    - project-docs/test-sql/2026-06-04_distributor_receiver_fields_hotfix.sql
--    - sql/2026-06-08_distributor_del_flag_hotfix.sql
--    - sql/2026-06-08_sys_user_scope_hotfix.sql
-- 2. All AppID/Secret/MchID/OpenID values here are TEST placeholders only.
-- 3. ID range 990000+ and TESTSP prefixes are reserved for this seed and can be deleted safely.
-- 4. For local dev login test:
--    /wxmini/login/test?appid=wx_test_sp_mch_9901
--    returns openId=test_openid_wx_test_sp_mch_9901 after the matching code change.
-- 5. After this seed, run:
--    project-docs/test-sql/2026-06-08_service_provider_seed_assertions.sql
--    and require every check_item to be PASS before release testing.

SET NAMES utf8mb4;
SET @pwd_admin123 := '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2';

START TRANSACTION;

-- -------------------------------------------------------------------
-- Cleanup for repeatable runs.
-- -------------------------------------------------------------------
DELETE FROM sys_user_role WHERE user_id BETWEEN 990001 AND 990099 OR role_id BETWEEN 9901 AND 9909;
DELETE FROM sys_role_menu WHERE role_id BETWEEN 9901 AND 9909;
DELETE FROM sys_user WHERE user_id BETWEEN 990001 AND 990099 OR user_name LIKE 'test_sp_%';
DELETE FROM sys_role WHERE role_id BETWEEN 9901 AND 9909 OR role_key LIKE 'TEST_SP_%';

DELETE FROM platform_transfer_record WHERE id BETWEEN 999501 AND 999599 OR transfer_no LIKE 'TESTSPTR%';
DELETE FROM distributor_settlement_record WHERE id BETWEEN 999001 AND 999099 OR settlement_no LIKE 'TESTSPDS%';
DELETE FROM order_profit_ledger WHERE id BETWEEN 998001 AND 998099 OR order_no LIKE 'TESTSP%';
DELETE FROM merchant_settlement_record WHERE id BETWEEN 997001 AND 997099 OR settlement_no LIKE 'TESTSPMS%';
DELETE FROM write_off_record WHERE id BETWEEN 996501 AND 996599 OR order_no LIKE 'TESTSP%';
DELETE FROM refund_record WHERE id BETWEEN 996001 AND 996099 OR refund_no LIKE 'TESTSPR%';
DELETE FROM payment_record WHERE id BETWEEN 995001 AND 995099 OR order_no LIKE 'TESTSP%';
DELETE FROM transaction_record WHERE id BETWEEN 994501 AND 994599 OR order_no LIKE 'TESTSP%';
DELETE FROM platform_income WHERE id BETWEEN 994401 AND 994499 OR order_no LIKE 'TESTSP%';
DELETE FROM order_item WHERE id BETWEEN 994101 AND 994199 OR order_no LIKE 'TESTSP%';
DELETE FROM mall_order WHERE id BETWEEN 994001 AND 994099 OR order_no LIKE 'TESTSP%';

DELETE FROM cart WHERE id BETWEEN 993901 AND 993999;
DELETE FROM user_coupon WHERE id BETWEEN 993801 AND 993899;
DELETE FROM coupon WHERE id BETWEEN 993701 AND 993799;
DELETE FROM product_image WHERE id BETWEEN 993501 AND 993699;
DELETE FROM groupon_activity_item WHERE id BETWEEN 993301 AND 993399;
DELETE FROM product WHERE id BETWEEN 993001 AND 993099 OR name LIKE 'TESTSP%';
DELETE FROM groupon_activity WHERE id BETWEEN 992001 AND 992099 OR name LIKE 'TESTSP%';
DELETE FROM product_category WHERE id BETWEEN 991501 AND 991599 OR name LIKE 'TESTSP%';
DELETE FROM merchant_user WHERE id BETWEEN 991401 AND 991499 OR username LIKE 'test_sp_mch%';
DELETE FROM merchant_store WHERE id BETWEEN 991301 AND 991399 OR name LIKE 'TESTSP%';
DELETE FROM merchant WHERE id BETWEEN 9901 AND 9909 OR name LIKE 'TESTSP%';
DELETE FROM distributor WHERE id BETWEEN 9901 AND 9909 OR username LIKE 'test_sp_dist%';
DELETE FROM mall_user WHERE id BETWEEN 991001 AND 991099 OR open_id LIKE 'test_openid_wx_test_sp_mch_%';
DELETE FROM user_info WHERE id BETWEEN 991001 AND 991099 OR open_id LIKE 'test_openid_wx_test_sp_mch_%';

-- -------------------------------------------------------------------
-- Backend roles and users.
-- Password for all test backend users: admin123
-- -------------------------------------------------------------------
INSERT INTO sys_role
(role_id, role_name, role_key, role_sort, data_scope, menu_check_strictly, dept_check_strictly,
 status, del_flag, create_by, create_time, role_scope, data_scope_type, distributor_id, remark)
VALUES
(9901, 'TEST SP Platform Auditor', 'TEST_SP_PLATFORM', 90, '1', 1, 1, '0', '0', 'seed', NOW(), 'PLATFORM', 'ALL', NULL, 'TEST seed role: platform can see all mall data'),
(9902, 'TEST SP Distributor 9901', 'TEST_SP_DISTRIBUTOR_9901', 91, '5', 1, 1, '0', '0', 'seed', NOW(), 'DISTRIBUTOR', 'DISTRIBUTOR_SELF', 9901, 'TEST seed role: distributor data scope'),
(9903, 'TEST SP Merchant 9901', 'TEST_SP_MERCHANT_9901', 92, '5', 1, 1, '0', '0', 'seed', NOW(), 'MERCHANT', 'MERCHANT_SELF', 9901, 'TEST seed role: merchant data scope')
ON DUPLICATE KEY UPDATE
  role_name = VALUES(role_name),
  role_scope = VALUES(role_scope),
  data_scope_type = VALUES(data_scope_type),
  distributor_id = VALUES(distributor_id),
  status = VALUES(status),
  update_by = 'seed',
  update_time = NOW();

INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT 9901, menu_id FROM sys_menu WHERE perms LIKE 'mall:%' OR path = 'mall';

INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT 9902, menu_id FROM sys_menu
WHERE perms IN (
  'mall:dashboard:list', 'mall:merchant:list', 'mall:merchant:query',
  'mall:product:list', 'mall:product:query', 'mall:product:add', 'mall:product:edit',
  'mall:groupon:list', 'mall:groupon:query', 'mall:groupon:add', 'mall:groupon:edit',
  'mall:order:list', 'mall:order:query', 'mall:finance:list', 'mall:settlement:list',
  'mall:after-sale:list'
) OR path IN ('mall', 'dashboard', 'merchant', 'product', 'order', 'finance', 'after-sale');

INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT 9903, menu_id FROM sys_menu
WHERE perms IN (
  'mall:dashboard:list',
  'mall:product:list', 'mall:product:query', 'mall:product:add', 'mall:product:edit',
  'mall:groupon:list', 'mall:groupon:query', 'mall:groupon:add', 'mall:groupon:edit',
  'mall:order:list', 'mall:order:query', 'mall:finance:list', 'mall:settlement:list',
  'mall:after-sale:list'
) OR path IN ('mall', 'dashboard', 'product', 'order', 'finance', 'after-sale');

INSERT INTO sys_user
(user_id, dept_id, user_name, nick_name, user_type, email, phonenumber, sex, password,
 status, del_flag, create_by, create_time, remark, account_type, distributor_id, merchant_id)
VALUES
(990001, 103, 'test_sp_platform', 'TEST SP Platform', '00', 'test_sp_platform@example.com', '13899010001', '0', @pwd_admin123, '0', '0', 'seed', NOW(), 'Platform test account. Password admin123.', 'PLATFORM', NULL, NULL),
(990002, 103, 'test_sp_dist_9901', 'TEST SP Dist 9901', '00', 'test_sp_dist_9901@example.com', '13899010002', '0', @pwd_admin123, '0', '0', 'seed', NOW(), 'Distributor scope account. Password admin123.', 'DISTRIBUTOR', 9901, NULL),
(990003, 103, 'test_sp_mch_9901', 'TEST SP Merchant 9901', '00', 'test_sp_mch_9901@example.com', '13899010003', '0', @pwd_admin123, '0', '0', 'seed', NOW(), 'Merchant scope account. Password admin123.', 'MERCHANT', 9901, 9901),
(990004, 103, 'test_sp_mch_9902', 'TEST SP Merchant 9902', '00', 'test_sp_mch_9902@example.com', '13899010004', '0', @pwd_admin123, '0', '0', 'seed', NOW(), 'Merchant scope account. Password admin123.', 'MERCHANT', 9901, 9902),
(990005, 103, 'test_sp_dist_9902', 'TEST SP Dist 9902', '00', 'test_sp_dist_9902@example.com', '13899010005', '0', @pwd_admin123, '0', '0', 'seed', NOW(), 'Second distributor scope account. Password admin123.', 'DISTRIBUTOR', 9902, NULL)
ON DUPLICATE KEY UPDATE
  nick_name = VALUES(nick_name),
  password = VALUES(password),
  status = VALUES(status),
  account_type = VALUES(account_type),
  distributor_id = VALUES(distributor_id),
  merchant_id = VALUES(merchant_id),
  update_by = 'seed',
  update_time = NOW();

INSERT IGNORE INTO sys_user_role (user_id, role_id) VALUES
(990001, 9901),
(990002, 9902),
(990003, 9903),
(990004, 9903),
(990005, 9902);

-- -------------------------------------------------------------------
-- Distributors.
-- -------------------------------------------------------------------
INSERT INTO distributor
(id, name, contact, phone, username, password, region_code, region_name, status,
 receiver_openid, receiver_type, remark, create_time, update_time)
VALUES
(9901, 'TESTSP Distributor North', 'TEST Dist North', '13899011001', 'test_sp_dist_north', @pwd_admin123, 'CN-NORTH', 'TEST North Region', 1, 'receiver_openid_dist_9901', 'WECHAT_BALANCE', 'Normal distributor with two merchants.', NOW(), NOW()),
(9902, 'TESTSP Distributor South', 'TEST Dist South', '13899011002', 'test_sp_dist_south', @pwd_admin123, 'CN-SOUTH', 'TEST South Region', 1, 'receiver_openid_dist_9902', 'WECHAT_BALANCE', 'Second distributor for cross-scope tests.', NOW(), NOW()),
(9903, 'TESTSP Distributor Disabled', 'TEST Dist Disabled', '13899011003', 'test_sp_dist_disabled', @pwd_admin123, 'CN-DISABLED', 'TEST Disabled Region', 0, 'receiver_openid_dist_9903', 'WECHAT_BALANCE', 'Disabled distributor for negative tests.', NOW(), NOW())
ON DUPLICATE KEY UPDATE
  name = VALUES(name),
  status = VALUES(status),
  receiver_openid = VALUES(receiver_openid),
  receiver_type = VALUES(receiver_type),
  update_time = NOW();

-- -------------------------------------------------------------------
-- Merchants and per-merchant miniapp/payment config.
-- Normal merchants: 9901, 9902, 9905.
-- Negative merchants: 9903 missing Secret/sub_mchid, 9904 stopped.
-- -------------------------------------------------------------------
INSERT INTO merchant
(id, distributor_id, name, logo, contact, phone, commission_rate, status, balance, total_income,
 address, avatar, description, business_hours, support_refund, support_booking, product_count, store_count,
 c_mini_app_id, c_mini_app_secret, m_mini_app_id, m_mini_app_secret, wx_pay_mch_id, wx_pay_api_key,
 receiver_openid, receiver_type, map_claim_status, map_poi_id, map_claim_url, map_claim_time,
 map_claim_remark, wx_applyment_id, wx_applyment_state, wx_applyment_reject_reason, wx_applyment_time,
 wx_applyment_finish_time, wx_payment_access_type, merchant_wx_mch_id, merchant_wx_mch_name,
 wx_profit_sharing_enabled, platform_receiver_mch_id, distributor_receiver_mch_id,
 merchant_share_rate, platform_share_rate, distributor_share_rate, settlement_cycle,
 create_by, create_time, update_by, update_time, remark, del_flag)
VALUES
(9901, 9901, 'TESTSP Merchant 9901 Coffee', '/profile/test/merchant_9901_logo.png', 'Alice 9901', '13899012001', 10.00, 1, 1200.00, 8800.00,
 'TEST City North Road 1', '/profile/test/merchant_9901_cover.png', 'Normal service-provider merchant for full pay/refund/settlement tests.', '09:00-22:00', 1, 1, 4, 2,
 'wx_test_sp_mch_9901', 'TEST_SECRET_9901_REPLACE_ME', 'wx_test_staff_9901', 'TEST_STAFF_SECRET_9901', NULL, NULL,
 'receiver_openid_mch_9901', 'WECHAT_BALANCE', 'CLAIMED', 'TEST_POI_9901', 'https://example.test/poi/9901', NOW(),
 'Map claim test data.', 'APPLYMENT_TEST_9901', 'FINISHED', NULL, DATE_SUB(NOW(), INTERVAL 15 DAY),
 DATE_SUB(NOW(), INTERVAL 14 DAY), 'EXISTING_MCH', '1900009901', 'TESTSP Merchant 9901 Sub Mch',
 1, '1900000000', '1900009901D', 82.00, 10.00, 8.00, 'T1',
 'seed', NOW(), 'seed', NOW(), 'Normal full-chain service-provider merchant.', '0'),
(9902, 9901, 'TESTSP Merchant 9902 Bakery', '/profile/test/merchant_9902_logo.png', 'Bob 9902', '13899012002', 12.00, 1, 980.00, 6600.00,
 'TEST City North Road 2', '/profile/test/merchant_9902_cover.png', 'Second merchant under distributor 9901 for same-distributor isolation tests.', '08:00-21:00', 1, 1, 4, 1,
 'wx_test_sp_mch_9902', 'TEST_SECRET_9902_REPLACE_ME', 'wx_test_staff_9902', 'TEST_STAFF_SECRET_9902', NULL, NULL,
 'receiver_openid_mch_9902', 'WECHAT_BALANCE', 'CLAIMED', 'TEST_POI_9902', 'https://example.test/poi/9902', NOW(),
 'Map claim test data.', 'APPLYMENT_TEST_9902', 'FINISHED', NULL, DATE_SUB(NOW(), INTERVAL 12 DAY),
 DATE_SUB(NOW(), INTERVAL 11 DAY), 'EXISTING_MCH', '1900009902', 'TESTSP Merchant 9902 Sub Mch',
 1, '1900000000', '1900009901D', 85.00, 10.00, 5.00, 'T1',
 'seed', NOW(), 'seed', NOW(), 'Normal merchant under the same distributor as 9901.', '0'),
(9903, 9902, 'TESTSP Merchant 9903 Missing Config', '/profile/test/merchant_9903_logo.png', 'Carol 9903', '13899012003', 10.00, 1, 0.00, 0.00,
 'TEST City South Road 3', '/profile/test/merchant_9903_cover.png', 'Negative test: has AppID but missing Secret and sub_mchid.', '10:00-20:00', 1, 1, 1, 1,
 'wx_test_sp_mch_9903', NULL, NULL, NULL, NULL, NULL,
 NULL, 'WECHAT_BALANCE', 'CLAIMED', NULL, NULL, NULL,
 'Negative config test.', NULL, 'NOT_SUBMITTED', NULL, NULL,
 NULL, 'EXISTING_MCH', NULL, NULL,
 0, NULL, NULL, 100.00, 0.00, 0.00, 'T1',
 'seed', NOW(), 'seed', NOW(), 'Expected login/pay/on-shelf checks to fail.', '0'),
(9904, 9902, 'TESTSP Merchant 9904 Stopped', '/profile/test/merchant_9904_logo.png', 'Dave 9904', '13899012004', 10.00, 3, 0.00, 0.00,
 'TEST City South Road 4', '/profile/test/merchant_9904_cover.png', 'Negative test: stopped merchant with otherwise complete payment config.', '09:30-18:30', 1, 1, 1, 1,
 'wx_test_sp_mch_9904', 'TEST_SECRET_9904_REPLACE_ME', NULL, NULL, NULL, NULL,
 'receiver_openid_mch_9904', 'WECHAT_BALANCE', 'CLAIMED', 'TEST_POI_9904', NULL, NOW(),
 'Stopped merchant negative test.', 'APPLYMENT_TEST_9904', 'FINISHED', NULL, DATE_SUB(NOW(), INTERVAL 20 DAY),
 DATE_SUB(NOW(), INTERVAL 19 DAY), 'EXISTING_MCH', '1900009904', 'TESTSP Merchant 9904 Sub Mch',
 1, '1900000000', '1900009902D', 90.00, 8.00, 2.00, 'T1',
 'seed', NOW(), 'seed', NOW(), 'Expected product/pay access to be blocked by status.', '0'),
(9905, NULL, 'TESTSP Merchant 9905 Direct Platform', '/profile/test/merchant_9905_logo.png', 'Eve 9905', '13899012005', 9.00, 1, 320.00, 2200.00,
 'TEST City Platform Road 5', '/profile/test/merchant_9905_cover.png', 'Normal platform-owned merchant without distributor share.', '09:00-23:00', 1, 1, 2, 1,
 'wx_test_sp_mch_9905', 'TEST_SECRET_9905_REPLACE_ME', NULL, NULL, NULL, NULL,
 'receiver_openid_mch_9905', 'WECHAT_BALANCE', 'CLAIMED', 'TEST_POI_9905', NULL, NOW(),
 'Platform-owned merchant test.', 'APPLYMENT_TEST_9905', 'FINISHED', NULL, DATE_SUB(NOW(), INTERVAL 10 DAY),
 DATE_SUB(NOW(), INTERVAL 9 DAY), 'EXISTING_MCH', '1900009905', 'TESTSP Merchant 9905 Sub Mch',
 1, '1900000000', NULL, 90.00, 10.00, 0.00, 'T1',
 'seed', NOW(), 'seed', NOW(), 'Normal service-provider merchant without distributor.', '0')
ON DUPLICATE KEY UPDATE
  distributor_id = VALUES(distributor_id),
  name = VALUES(name),
  status = VALUES(status),
  c_mini_app_id = VALUES(c_mini_app_id),
  c_mini_app_secret = VALUES(c_mini_app_secret),
  merchant_wx_mch_id = VALUES(merchant_wx_mch_id),
  wx_profit_sharing_enabled = VALUES(wx_profit_sharing_enabled),
  platform_receiver_mch_id = VALUES(platform_receiver_mch_id),
  distributor_receiver_mch_id = VALUES(distributor_receiver_mch_id),
  merchant_share_rate = VALUES(merchant_share_rate),
  platform_share_rate = VALUES(platform_share_rate),
  distributor_share_rate = VALUES(distributor_share_rate),
  update_by = 'seed',
  update_time = NOW(),
  del_flag = '0';

-- -------------------------------------------------------------------
-- Stores and merchant miniapp staff users.
-- Merchant miniapp/staff password: admin123
-- -------------------------------------------------------------------
INSERT INTO merchant_store
(id, merchant_id, name, contact, phone, address, longitude, latitude, business_hours, avatar, status, is_main, create_by, create_time, update_by, update_time, del_flag)
VALUES
(991301, 9901, 'TESTSP Coffee Main', 'Alice 9901', '13899013001', 'TEST City North Road 1', 116.4100000, 39.9000000, '09:00-22:00', '/profile/test/store_991301.png', 1, 1, 'seed', NOW(), 'seed', NOW(), '0'),
(991302, 9901, 'TESTSP Coffee Branch', 'Alice 9901', '13899013002', 'TEST City North Road 1 Branch', 116.4200000, 39.9100000, '10:00-20:00', '/profile/test/store_991302.png', 1, 0, 'seed', NOW(), 'seed', NOW(), '0'),
(991303, 9902, 'TESTSP Bakery Main', 'Bob 9902', '13899013003', 'TEST City North Road 2', 116.4300000, 39.9200000, '08:00-21:00', '/profile/test/store_991303.png', 1, 1, 'seed', NOW(), 'seed', NOW(), '0'),
(991304, 9903, 'TESTSP Missing Config Store', 'Carol 9903', '13899013004', 'TEST City South Road 3', 116.4400000, 39.9300000, '10:00-20:00', '/profile/test/store_991304.png', 1, 1, 'seed', NOW(), 'seed', NOW(), '0'),
(991305, 9904, 'TESTSP Stopped Store', 'Dave 9904', '13899013005', 'TEST City South Road 4', 116.4500000, 39.9400000, '09:30-18:30', '/profile/test/store_991305.png', 1, 1, 'seed', NOW(), 'seed', NOW(), '0'),
(991306, 9905, 'TESTSP Platform Merchant Store', 'Eve 9905', '13899013006', 'TEST City Platform Road 5', 116.4600000, 39.9500000, '09:00-23:00', '/profile/test/store_991306.png', 1, 1, 'seed', NOW(), 'seed', NOW(), '0')
ON DUPLICATE KEY UPDATE name = VALUES(name), status = VALUES(status), update_time = NOW(), del_flag = '0';

INSERT INTO merchant_user
(id, merchant_id, username, password, real_name, phone, role, status, create_by, create_time, update_by, update_time, remark, del_flag)
VALUES
(991401, 9901, 'test_sp_mch9901_owner', @pwd_admin123, 'Owner 9901', '13899014001', 'owner', 1, 'seed', NOW(), 'seed', NOW(), 'Merchant miniapp owner. Password admin123.', '0'),
(991402, 9901, 'test_sp_mch9901_staff', @pwd_admin123, 'Staff 9901', '13899014002', 'member', 1, 'seed', NOW(), 'seed', NOW(), 'Merchant miniapp staff. Password admin123.', '0'),
(991403, 9902, 'test_sp_mch9902_owner', @pwd_admin123, 'Owner 9902', '13899014003', 'owner', 1, 'seed', NOW(), 'seed', NOW(), 'Merchant miniapp owner. Password admin123.', '0'),
(991404, 9905, 'test_sp_mch9905_owner', @pwd_admin123, 'Owner 9905', '13899014004', 'owner', 1, 'seed', NOW(), 'seed', NOW(), 'Merchant miniapp owner. Password admin123.', '0')
ON DUPLICATE KEY UPDATE password = VALUES(password), status = VALUES(status), update_time = NOW(), del_flag = '0';

-- -------------------------------------------------------------------
-- Miniapp users. IDs are mirrored to mall_user so backend order list can display names.
-- -------------------------------------------------------------------
INSERT INTO user_info
(id, user_id, user_name, user_type, phone, open_id, union_id, avatar_url, create_time, update_time, del_flag)
VALUES
(991001, 'test_user_9901_a', 'TEST Buyer 9901 A', '0', '15099010001', 'test_openid_wx_test_sp_mch_9901', 'test_union_9901_a', 'https://example.test/avatar/9901a.png', NOW(), NOW(), '0'),
(991002, 'test_user_9901_b', 'TEST Buyer 9901 B', '0', '15099010002', 'test_openid_wx_test_sp_mch_9901_b', 'test_union_9901_b', 'https://example.test/avatar/9901b.png', NOW(), NOW(), '0'),
(991003, 'test_user_9902_a', 'TEST Buyer 9902 A', '0', '15099010003', 'test_openid_wx_test_sp_mch_9902', 'test_union_9902_a', 'https://example.test/avatar/9902a.png', NOW(), NOW(), '0'),
(991004, 'test_user_9903_a', 'TEST Buyer 9903 A', '0', '15099010004', 'test_openid_wx_test_sp_mch_9903', 'test_union_9903_a', 'https://example.test/avatar/9903a.png', NOW(), NOW(), '0'),
(991005, 'test_user_9905_a', 'TEST Buyer 9905 A', '0', '15099010005', 'test_openid_wx_test_sp_mch_9905', 'test_union_9905_a', 'https://example.test/avatar/9905a.png', NOW(), NOW(), '0')
ON DUPLICATE KEY UPDATE
  user_id = VALUES(user_id),
  user_name = VALUES(user_name),
  phone = VALUES(phone),
  union_id = VALUES(union_id),
  avatar_url = VALUES(avatar_url),
  update_time = NOW(),
  del_flag = '0';

INSERT INTO mall_user
(id, nickname, phone, avatar, gender, city, open_id, status, total_orders, total_amount, create_time, update_time, remark)
VALUES
(991001, 'TEST Buyer 9901 A', '15099010001', 'https://example.test/avatar/9901a.png', 1, 'TEST City', 'test_openid_wx_test_sp_mch_9901', 1, 7, 467.66, NOW(), NOW(), 'Mirrors user_info for backend order joins.'),
(991002, 'TEST Buyer 9901 B', '15099010002', 'https://example.test/avatar/9901b.png', 2, 'TEST City', 'test_openid_wx_test_sp_mch_9901_b', 1, 2, 108.87, NOW(), NOW(), 'Mirrors user_info for backend order joins.'),
(991003, 'TEST Buyer 9902 A', '15099010003', 'https://example.test/avatar/9902a.png', 1, 'TEST City', 'test_openid_wx_test_sp_mch_9902', 1, 4, 249.90, NOW(), NOW(), 'Mirrors user_info for backend order joins.'),
(991004, 'TEST Buyer 9903 A', '15099010004', 'https://example.test/avatar/9903a.png', 1, 'TEST City', 'test_openid_wx_test_sp_mch_9903', 1, 0, 0.00, NOW(), NOW(), 'Negative config buyer.'),
(991005, 'TEST Buyer 9905 A', '15099010005', 'https://example.test/avatar/9905a.png', 2, 'TEST City', 'test_openid_wx_test_sp_mch_9905', 1, 2, 68.01, NOW(), NOW(), 'Platform merchant buyer.')
ON DUPLICATE KEY UPDATE nickname = VALUES(nickname), phone = VALUES(phone), total_orders = VALUES(total_orders), total_amount = VALUES(total_amount), update_time = NOW();

-- -------------------------------------------------------------------
-- Products, groupon activities and coupons.
-- -------------------------------------------------------------------
INSERT INTO product_category
(id, merchant_id, name, sort, status, create_by, create_time, update_by, update_time, remark, del_flag)
VALUES
(991501, 9901, 'TESTSP Coffee Deals', 100, 1, 'seed', NOW(), 'seed', NOW(), 'Seed category.', '0'),
(991502, 9901, 'TESTSP Coffee Edge Amounts', 90, 1, 'seed', NOW(), 'seed', NOW(), 'Seed category for fen conversion.', '0'),
(991503, 9902, 'TESTSP Bakery Deals', 100, 1, 'seed', NOW(), 'seed', NOW(), 'Seed category.', '0'),
(991504, 9903, 'TESTSP Missing Config Deals', 100, 1, 'seed', NOW(), 'seed', NOW(), 'Negative category.', '0'),
(991505, 9904, 'TESTSP Stopped Deals', 100, 1, 'seed', NOW(), 'seed', NOW(), 'Stopped merchant category.', '0'),
(991506, 9905, 'TESTSP Platform Deals', 100, 1, 'seed', NOW(), 'seed', NOW(), 'Platform-owned merchant category.', '0')
ON DUPLICATE KEY UPDATE name = VALUES(name), status = VALUES(status), update_time = NOW(), del_flag = '0';

INSERT INTO groupon_activity
(id, merchant_id, distributor_id, name, cover_image, poster_image, detail_images, description,
 start_time, end_time, status, total_sold, limit_per_user, sort, source_type, create_time, update_time, del_flag)
VALUES
(992001, 9901, 9901, 'TESTSP 9901 Full Chain Activity', '/profile/test/groupon_992001.png', '/profile/test/groupon_992001_poster.png', '[]', 'Normal activity for payment/refund/write-off tests.', DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_ADD(NOW(), INTERVAL 30 DAY), 1, 35, 5, 100, 'ADMIN', NOW(), NOW(), '0'),
(992002, 9902, 9901, 'TESTSP 9902 Bakery Activity', '/profile/test/groupon_992002.png', '/profile/test/groupon_992002_poster.png', '[]', 'Second merchant activity.', DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_ADD(NOW(), INTERVAL 30 DAY), 1, 18, 3, 90, 'ADMIN', NOW(), NOW(), '0'),
(992003, 9903, 9902, 'TESTSP 9903 Config Failure Activity', '/profile/test/groupon_992003.png', NULL, '[]', 'Should fail pay due merchant config.', DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 30 DAY), 1, 0, 1, 80, 'ADMIN', NOW(), NOW(), '0'),
(992004, 9904, 9902, 'TESTSP 9904 Stopped Activity', '/profile/test/groupon_992004.png', NULL, '[]', 'Should be blocked because merchant stopped.', DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 30 DAY), 1, 0, 1, 70, 'ADMIN', NOW(), NOW(), '0'),
(992005, 9905, NULL, 'TESTSP 9905 Platform Activity', '/profile/test/groupon_992005.png', NULL, '[]', 'Platform-owned merchant activity.', DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 30 DAY), 1, 5, 2, 60, 'ADMIN', NOW(), NOW(), '0')
ON DUPLICATE KEY UPDATE name = VALUES(name), status = VALUES(status), update_time = NOW(), del_flag = '0';

INSERT INTO product
(id, merchant_id, category_id, groupon_id, name, cover_image, images, main_image, price, original_price,
 stock, sales, status, verify_type, valid_days, verify_notice, description, store_ids, sort,
 create_by, create_time, update_by, update_time, remark, del_flag)
VALUES
(993001, 9901, 991501, 992001, 'TESTSP 9901 Coffee Coupon 0.01', '/profile/test/product_993001.png', '["/profile/test/product_993001.png"]', '/profile/test/product_993001.png', 0.01, 9.90, 100, 3, 1, 1, 7, 'Show code before use.', 'Fen conversion smoke item.', '[991301,991302]', 100, 'seed', NOW(), 'seed', NOW(), 'One-cent payment test item.', '0'),
(993002, 9901, 991501, 992001, 'TESTSP 9901 Latte Set 9.99', '/profile/test/product_993002.png', '["/profile/test/product_993002.png"]', '/profile/test/product_993002.png', 9.99, 19.90, 200, 28, 1, 1, 7, 'Valid for both stores.', 'Normal low amount item.', '[991301,991302]', 95, 'seed', NOW(), 'seed', NOW(), 'Low amount item.', '0'),
(993003, 9901, 991502, 992001, 'TESTSP 9901 Family Set 199.99', '/profile/test/product_993003.png', '["/profile/test/product_993003.png"]', '/profile/test/product_993003.png', 199.99, 299.00, 50, 12, 1, 1, 30, 'Reservation recommended.', 'High precision amount item.', '[991301]', 90, 'seed', NOW(), 'seed', NOW(), 'Amount rounding test item.', '0'),
(993004, 9901, 991502, NULL, 'TESTSP 9901 Offline Hidden Item', '/profile/test/product_993004.png', '[]', '/profile/test/product_993004.png', 49.90, 88.00, 20, 0, 0, 1, 7, 'Hidden item.', 'Should not appear in normal miniapp list.', '[991301]', 10, 'seed', NOW(), 'seed', NOW(), 'Off-shelf negative item.', '0'),
(993011, 9902, 991503, 992002, 'TESTSP 9902 Bread Set 19.90', '/profile/test/product_993011.png', '["/profile/test/product_993011.png"]', '/profile/test/product_993011.png', 19.90, 29.90, 80, 14, 1, 1, 5, 'Use at main store.', 'Second merchant item.', '[991303]', 100, 'seed', NOW(), 'seed', NOW(), 'Same distributor cross-merchant test item.', '0'),
(993012, 9902, 991503, 992002, 'TESTSP 9902 Cake Set 88.88', '/profile/test/product_993012.png', '["/profile/test/product_993012.png"]', '/profile/test/product_993012.png', 88.88, 128.00, 40, 9, 1, 1, 10, 'Reserve one day earlier.', 'Coupon and settlement item.', '[991303]', 90, 'seed', NOW(), 'seed', NOW(), 'Amount rounding test item.', '0'),
(993021, 9903, 991504, 992003, 'TESTSP 9903 Config Missing Item', '/profile/test/product_993021.png', '[]', '/profile/test/product_993021.png', 29.90, 49.90, 10, 0, 1, 1, 7, 'Should fail before pay.', 'Negative config item.', '[991304]', 100, 'seed', NOW(), 'seed', NOW(), 'Expected pay config failure.', '0'),
(993031, 9904, 991505, 992004, 'TESTSP 9904 Stopped Merchant Item', '/profile/test/product_993031.png', '[]', '/profile/test/product_993031.png', 39.90, 59.90, 10, 0, 1, 1, 7, 'Should be blocked.', 'Stopped merchant item.', '[991305]', 100, 'seed', NOW(), 'seed', NOW(), 'Expected merchant status failure.', '0'),
(993041, 9905, 991506, 992005, 'TESTSP 9905 Platform Item 68.01', '/profile/test/product_993041.png', '["/profile/test/product_993041.png"]', '/profile/test/product_993041.png', 68.01, 88.00, 30, 4, 1, 1, 7, 'Use at platform merchant store.', 'No distributor share item.', '[991306]', 100, 'seed', NOW(), 'seed', NOW(), 'Platform-owned merchant settlement item.', '0')
ON DUPLICATE KEY UPDATE
  name = VALUES(name),
  price = VALUES(price),
  original_price = VALUES(original_price),
  stock = VALUES(stock),
  sales = VALUES(sales),
  status = VALUES(status),
  update_time = NOW(),
  del_flag = '0';

INSERT INTO product_image
(id, product_id, merchant_id, image_type, image_url, sort_order, sku_value, status, create_by, create_time, update_by, update_time)
VALUES
(993501, 993001, 9901, 'main', '/profile/test/product_993001.png', 1, NULL, 1, 'seed', NOW(), 'seed', NOW()),
(993502, 993002, 9901, 'main', '/profile/test/product_993002.png', 1, NULL, 1, 'seed', NOW(), 'seed', NOW()),
(993503, 993003, 9901, 'main', '/profile/test/product_993003.png', 1, NULL, 1, 'seed', NOW(), 'seed', NOW()),
(993511, 993011, 9902, 'main', '/profile/test/product_993011.png', 1, NULL, 1, 'seed', NOW(), 'seed', NOW()),
(993512, 993012, 9902, 'main', '/profile/test/product_993012.png', 1, NULL, 1, 'seed', NOW(), 'seed', NOW()),
(993541, 993041, 9905, 'main', '/profile/test/product_993041.png', 1, NULL, 1, 'seed', NOW(), 'seed', NOW())
ON DUPLICATE KEY UPDATE image_url = VALUES(image_url), status = VALUES(status), update_time = NOW();

INSERT INTO groupon_activity_item
(id, merchant_id, distributor_id, groupon_id, name, title, content, description, cover_image, detail_images,
 original_price, groupon_price, discount_rate, stock, sales, limit_per_user, valid_days, store_ids,
 dish_groups, dish_total_price, direct_total_price, dish_count, available_dish_count, status, sort,
 del_flag, create_time, update_time)
VALUES
(993301, 9901, 9901, 992001, 'TESTSP 9901 Coffee Combo', 'Coffee combo', 'Latte x1; snack x1', 'Normal groupon item.', '/profile/test/groupon_item_993301.png', '[]', 29.90, 9.99, 3.34, 100, 18, 5, 7, '[991301,991302]', '[]', 999, 1, 2, 2, 1, 100, '0', NOW(), NOW()),
(993302, 9901, 9901, 992001, 'TESTSP 9901 Family Combo', 'Family combo', 'Family package.', 'High amount groupon item.', '/profile/test/groupon_item_993302.png', '[]', 299.00, 199.99, 6.69, 30, 6, 2, 30, '[991301]', '[]', 19999, 1, 5, 5, 1, 90, '0', NOW(), NOW()),
(993311, 9902, 9901, 992002, 'TESTSP 9902 Bakery Combo', 'Bakery combo', 'Bread and drink.', 'Second merchant groupon item.', '/profile/test/groupon_item_993311.png', '[]', 59.90, 19.90, 3.32, 50, 7, 3, 5, '[991303]', '[]', 1990, 1, 2, 2, 1, 100, '0', NOW(), NOW()),
(993341, 9905, NULL, 992005, 'TESTSP 9905 Platform Combo', 'Platform merchant combo', 'No distributor share.', 'Platform merchant groupon item.', '/profile/test/groupon_item_993341.png', '[]', 88.00, 68.01, 7.73, 20, 2, 2, 7, '[991306]', '[]', 6801, 1, 1, 1, 1, 100, '0', NOW(), NOW())
ON DUPLICATE KEY UPDATE name = VALUES(name), groupon_price = VALUES(groupon_price), status = VALUES(status), update_time = NOW(), del_flag = '0';

INSERT INTO coupon
(id, merchant_id, name, type, discount_value, min_amount, total_count, used_count, start_time, end_time, status, create_time, update_time, del_flag)
VALUES
(993701, NULL, 'TESTSP Platform 5 Off 50', 1, 5.00, 50.00, 1000, 2, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 30 DAY), 1, NOW(), NOW(), '0'),
(993702, 9901, 'TESTSP 9901 10 Off 100', 1, 10.00, 100.00, 300, 1, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 30 DAY), 1, NOW(), NOW(), '0'),
(993703, 9902, 'TESTSP 9902 8 Off 80', 1, 8.00, 80.00, 200, 1, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 30 DAY), 1, NOW(), NOW(), '0')
ON DUPLICATE KEY UPDATE name = VALUES(name), status = VALUES(status), update_time = NOW(), del_flag = '0';

INSERT INTO user_coupon
(id, user_id, coupon_id, merchant_id, status, use_time, order_no, create_time, del_flag)
VALUES
(993801, 991001, 993702, 9901, 1, DATE_SUB(NOW(), INTERVAL 1 DAY), 'TESTSP9901007', DATE_SUB(NOW(), INTERVAL 2 DAY), '0'),
(993802, 991003, 993703, 9902, 0, NULL, '', NOW(), '0'),
(993803, 991005, 993701, NULL, 0, NULL, '', NOW(), '0')
ON DUPLICATE KEY UPDATE status = VALUES(status), use_time = VALUES(use_time), order_no = VALUES(order_no), del_flag = '0';

INSERT INTO cart
(id, user_id, product_id, merchant_id, quantity, checked, create_time, update_time, del_flag)
VALUES
(993901, 991001, 993002, 9901, 1, 1, NOW(), NOW(), '0'),
(993902, 991003, 993011, 9902, 2, 1, NOW(), NOW(), '0'),
(993903, 991005, 993041, 9905, 1, 1, NOW(), NOW(), '0')
ON DUPLICATE KEY UPDATE quantity = VALUES(quantity), checked = VALUES(checked), update_time = NOW(), del_flag = '0';

-- -------------------------------------------------------------------
-- Orders and items.
-- mall_order.status: 0 pending, 1 paid, 2 used, 3 completed, 4 refunded, 5 cancelled.
-- -------------------------------------------------------------------
INSERT INTO mall_order
(id, order_no, merchant_id, user_id, store_id, total_amount, pay_amount, commission, merchant_income,
 coupon_id, coupon_amount, groupon_id, status, write_off_code, write_off_status, write_off_time,
 write_off_user_id, valid_days, pay_time, use_time, complete_time, cancel_time, refund_time,
 create_by, create_time, update_by, update_time, remark, del_flag)
VALUES
(994001, 'TESTSP9901001', 9901, 991001, 991301, 0.01, 0.01, 0.00, 0.01, NULL, 0.00, 992001, 0, 'WO9901001', 0, NULL, NULL, 7, NULL, NULL, NULL, NULL, NULL, 'seed', DATE_SUB(NOW(), INTERVAL 30 MINUTE), 'seed', NOW(), 'Pending one-cent order.', '0'),
(994002, 'TESTSP9901002', 9901, 991001, 991301, 9.99, 9.99, 1.00, 8.19, NULL, 0.00, 992001, 1, 'WO9901002', 0, NULL, NULL, 7, DATE_SUB(NOW(), INTERVAL 3 HOUR), NULL, NULL, NULL, NULL, 'seed', DATE_SUB(NOW(), INTERVAL 4 HOUR), 'seed', NOW(), 'Paid order waiting write-off.', '0'),
(994003, 'TESTSP9901003', 9901, 991001, 991302, 99.90, 99.90, 9.99, 81.92, NULL, 0.00, 992001, 2, 'WO9901003', 1, DATE_SUB(NOW(), INTERVAL 1 DAY), 991402, 7, DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY), NULL, NULL, NULL, 'seed', DATE_SUB(NOW(), INTERVAL 2 DAY), 'seed', NOW(), 'Used order for refund approval test.', '0'),
(994004, 'TESTSP9901004', 9901, 991002, 991301, 199.99, 199.99, 20.00, 163.99, NULL, 0.00, 992001, 3, 'WO9901004', 1, DATE_SUB(NOW(), INTERVAL 3 DAY), 991401, 30, DATE_SUB(NOW(), INTERVAL 4 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY), NULL, NULL, 'seed', DATE_SUB(NOW(), INTERVAL 4 DAY), 'seed', NOW(), 'Completed order with ARRIVED settlement.', '0'),
(994005, 'TESTSP9901005', 9901, 991002, 991301, 19.90, 19.90, 1.99, 16.32, NULL, 0.00, 992001, 4, 'WO9901005', 0, NULL, NULL, 7, DATE_SUB(NOW(), INTERVAL 5 DAY), NULL, NULL, NULL, DATE_SUB(NOW(), INTERVAL 4 DAY), 'seed', DATE_SUB(NOW(), INTERVAL 5 DAY), 'seed', NOW(), 'Refunded order.', '0'),
(994006, 'TESTSP9901006', 9901, 991001, 991301, 49.90, 49.90, 4.99, 40.92, NULL, 0.00, NULL, 5, 'WO9901006', 0, NULL, NULL, 7, NULL, NULL, NULL, DATE_SUB(NOW(), INTERVAL 1 HOUR), NULL, 'seed', DATE_SUB(NOW(), INTERVAL 2 HOUR), 'seed', NOW(), 'Cancelled order with closed payment.', '0'),
(994007, 'TESTSP9901007', 9901, 991001, 991301, 99.90, 88.88, 8.89, 72.88, 993702, 11.02, 992001, 1, 'WO9901007', 0, NULL, NULL, 7, DATE_SUB(NOW(), INTERVAL 1 HOUR), NULL, NULL, NULL, NULL, 'seed', DATE_SUB(NOW(), INTERVAL 2 HOUR), 'seed', NOW(), 'Paid order with coupon and rounding.', '0'),
(994011, 'TESTSP9902001', 9902, 991003, 991303, 19.90, 19.90, 1.99, 16.92, NULL, 0.00, 992002, 0, 'WO9902001', 0, NULL, NULL, 5, NULL, NULL, NULL, NULL, NULL, 'seed', DATE_SUB(NOW(), INTERVAL 20 MINUTE), 'seed', NOW(), 'Second merchant pending order.', '0'),
(994012, 'TESTSP9902002', 9902, 991003, 991303, 88.88, 80.88, 8.09, 68.75, 993703, 8.00, 992002, 1, 'WO9902002', 0, NULL, NULL, 10, DATE_SUB(NOW(), INTERVAL 2 HOUR), NULL, NULL, NULL, NULL, 'seed', DATE_SUB(NOW(), INTERVAL 3 HOUR), 'seed', NOW(), 'Second merchant paid order.', '0'),
(994013, 'TESTSP9902003', 9902, 991003, 991303, 88.88, 88.88, 8.89, 75.55, NULL, 0.00, 992002, 3, 'WO9902003', 1, DATE_SUB(NOW(), INTERVAL 2 DAY), 991403, 10, DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY), NULL, NULL, 'seed', DATE_SUB(NOW(), INTERVAL 3 DAY), 'seed', NOW(), 'Second merchant completed order.', '0'),
(994014, 'TESTSP9902004', 9902, 991003, 991303, 19.90, 19.90, 1.99, 16.92, NULL, 0.00, 992002, 4, 'WO9902004', 0, NULL, NULL, 5, DATE_SUB(NOW(), INTERVAL 5 DAY), NULL, NULL, NULL, DATE_SUB(NOW(), INTERVAL 4 DAY), 'seed', DATE_SUB(NOW(), INTERVAL 5 DAY), 'seed', NOW(), 'Second merchant refunded order.', '0'),
(994021, 'TESTSP9905001', 9905, 991005, 991306, 68.01, 68.01, 6.80, 61.21, NULL, 0.00, 992005, 3, 'WO9905001', 1, DATE_SUB(NOW(), INTERVAL 1 DAY), 991404, 7, DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 12 HOUR), NULL, NULL, 'seed', DATE_SUB(NOW(), INTERVAL 2 DAY), 'seed', NOW(), 'Platform-owned merchant completed order.', '0'),
(994031, 'TESTSP9903001', 9903, 991004, 991304, 29.90, 29.90, 0.00, 29.90, NULL, 0.00, 992003, 0, 'WO9903001', 0, NULL, NULL, 7, NULL, NULL, NULL, NULL, NULL, 'seed', DATE_SUB(NOW(), INTERVAL 10 MINUTE), 'seed', NOW(), 'Negative order: merchant payment config missing.', '0')
ON DUPLICATE KEY UPDATE
  status = VALUES(status),
  pay_amount = VALUES(pay_amount),
  commission = VALUES(commission),
  merchant_income = VALUES(merchant_income),
  write_off_status = VALUES(write_off_status),
  write_off_time = VALUES(write_off_time),
  pay_time = VALUES(pay_time),
  use_time = VALUES(use_time),
  complete_time = VALUES(complete_time),
  cancel_time = VALUES(cancel_time),
  refund_time = VALUES(refund_time),
  update_time = NOW(),
  del_flag = '0';

INSERT INTO order_item
(id, order_id, order_no, merchant_id, product_id, product_name, product_image, price, quantity, subtotal, create_time, del_flag)
VALUES
(994101, 994001, 'TESTSP9901001', 9901, 993001, 'TESTSP 9901 Coffee Coupon 0.01', '/profile/test/product_993001.png', 0.01, 1, 0.01, NOW(), '0'),
(994102, 994002, 'TESTSP9901002', 9901, 993002, 'TESTSP 9901 Latte Set 9.99', '/profile/test/product_993002.png', 9.99, 1, 9.99, NOW(), '0'),
(994103, 994003, 'TESTSP9901003', 9901, 993002, 'TESTSP 9901 Latte Set 9.99', '/profile/test/product_993002.png', 9.99, 10, 99.90, NOW(), '0'),
(994104, 994004, 'TESTSP9901004', 9901, 993003, 'TESTSP 9901 Family Set 199.99', '/profile/test/product_993003.png', 199.99, 1, 199.99, NOW(), '0'),
(994105, 994005, 'TESTSP9901005', 9901, 993002, 'TESTSP 9901 Latte Set 9.99', '/profile/test/product_993002.png', 9.95, 2, 19.90, NOW(), '0'),
(994106, 994006, 'TESTSP9901006', 9901, 993004, 'TESTSP 9901 Offline Hidden Item', '/profile/test/product_993004.png', 49.90, 1, 49.90, NOW(), '0'),
(994107, 994007, 'TESTSP9901007', 9901, 993002, 'TESTSP 9901 Latte Set 9.99', '/profile/test/product_993002.png', 9.99, 10, 99.90, NOW(), '0'),
(994111, 994011, 'TESTSP9902001', 9902, 993011, 'TESTSP 9902 Bread Set 19.90', '/profile/test/product_993011.png', 19.90, 1, 19.90, NOW(), '0'),
(994112, 994012, 'TESTSP9902002', 9902, 993012, 'TESTSP 9902 Cake Set 88.88', '/profile/test/product_993012.png', 88.88, 1, 88.88, NOW(), '0'),
(994113, 994013, 'TESTSP9902003', 9902, 993012, 'TESTSP 9902 Cake Set 88.88', '/profile/test/product_993012.png', 88.88, 1, 88.88, NOW(), '0'),
(994114, 994014, 'TESTSP9902004', 9902, 993011, 'TESTSP 9902 Bread Set 19.90', '/profile/test/product_993011.png', 19.90, 1, 19.90, NOW(), '0'),
(994121, 994021, 'TESTSP9905001', 9905, 993041, 'TESTSP 9905 Platform Item 68.01', '/profile/test/product_993041.png', 68.01, 1, 68.01, NOW(), '0'),
(994131, 994031, 'TESTSP9903001', 9903, 993021, 'TESTSP 9903 Config Missing Item', '/profile/test/product_993021.png', 29.90, 1, 29.90, NOW(), '0')
ON DUPLICATE KEY UPDATE quantity = VALUES(quantity), subtotal = VALUES(subtotal), del_flag = '0';

-- -------------------------------------------------------------------
-- Payment, refund and money ledger data.
-- payment_record.pay_status: 0 pending, 1 success, 2 failed, 3 closed.
-- refund_record.status: 1 pending, 2 approved/waiting WeChat refund, 3 rejected, 4 refunded, 5 abnormal.
-- -------------------------------------------------------------------
INSERT INTO payment_record
(id, order_no, merchant_id, user_id, amount, pay_type, transaction_id, out_trade_no, pay_status, pay_time, notify_result, create_time, update_time, del_flag)
VALUES
(995001, 'TESTSP9901001', 9901, 991001, 0.01, 'wechat_partner', '', 'TESTSP9901001', 0, NULL, '', NOW(), NOW(), '0'),
(995002, 'TESTSP9901002', 9901, 991001, 9.99, 'wechat_partner', '4200009901002', 'TESTSP9901002', 1, DATE_SUB(NOW(), INTERVAL 3 HOUR), 'SUCCESS_PARTNER', NOW(), NOW(), '0'),
(995003, 'TESTSP9901003', 9901, 991001, 99.90, 'wechat_partner', '4200009901003', 'TESTSP9901003', 1, DATE_SUB(NOW(), INTERVAL 2 DAY), 'SUCCESS_PARTNER', NOW(), NOW(), '0'),
(995004, 'TESTSP9901004', 9901, 991002, 199.99, 'wechat_partner', '4200009901004', 'TESTSP9901004', 1, DATE_SUB(NOW(), INTERVAL 4 DAY), 'SUCCESS_PARTNER', NOW(), NOW(), '0'),
(995005, 'TESTSP9901005', 9901, 991002, 19.90, 'wechat_partner', '4200009901005', 'TESTSP9901005', 1, DATE_SUB(NOW(), INTERVAL 5 DAY), 'SUCCESS_PARTNER', NOW(), NOW(), '0'),
(995006, 'TESTSP9901006', 9901, 991001, 49.90, 'wechat_partner', '', 'TESTSP9901006', 3, NULL, 'CLOSED', NOW(), NOW(), '0'),
(995007, 'TESTSP9901007', 9901, 991001, 88.88, 'wechat_partner', '4200009901007', 'TESTSP9901007', 1, DATE_SUB(NOW(), INTERVAL 1 HOUR), 'SUCCESS_PARTNER', NOW(), NOW(), '0'),
(995011, 'TESTSP9902001', 9902, 991003, 19.90, 'wechat_partner', '', 'TESTSP9902001', 0, NULL, '', NOW(), NOW(), '0'),
(995012, 'TESTSP9902002', 9902, 991003, 80.88, 'wechat_partner', '4200009902002', 'TESTSP9902002', 1, DATE_SUB(NOW(), INTERVAL 2 HOUR), 'SUCCESS_PARTNER', NOW(), NOW(), '0'),
(995013, 'TESTSP9902003', 9902, 991003, 88.88, 'wechat_partner', '4200009902003', 'TESTSP9902003', 1, DATE_SUB(NOW(), INTERVAL 3 DAY), 'SUCCESS_PARTNER', NOW(), NOW(), '0'),
(995014, 'TESTSP9902004', 9902, 991003, 19.90, 'wechat_partner', '4200009902004', 'TESTSP9902004', 1, DATE_SUB(NOW(), INTERVAL 5 DAY), 'SUCCESS_PARTNER', NOW(), NOW(), '0'),
(995021, 'TESTSP9905001', 9905, 991005, 68.01, 'wechat_partner', '4200009905001', 'TESTSP9905001', 1, DATE_SUB(NOW(), INTERVAL 2 DAY), 'SUCCESS_PARTNER', NOW(), NOW(), '0'),
(995031, 'TESTSP9903001', 9903, 991004, 29.90, 'wechat_partner', '', 'TESTSP9903001', 2, NULL, 'CONFIG_MISSING', NOW(), NOW(), '0')
ON DUPLICATE KEY UPDATE pay_status = VALUES(pay_status), transaction_id = VALUES(transaction_id), pay_time = VALUES(pay_time), notify_result = VALUES(notify_result), update_time = NOW(), del_flag = '0';

INSERT INTO refund_record
(id, order_no, refund_no, merchant_id, user_id, payment_record_id, refund_amount, refund_reason, refund_type,
 status, audit_time, refund_time, reject_reason, operator, create_time, update_time, del_flag)
VALUES
(996001, 'TESTSP9901002', 'TESTSPR9901002P', 9901, 991001, 995002, 9.99, 'Pending user refund test.', 1, 1, NULL, NULL, '', '', DATE_SUB(NOW(), INTERVAL 20 MINUTE), NOW(), '0'),
(996002, 'TESTSP9901003', 'TESTSPR9901003A', 9901, 991001, 995003, 99.90, 'Approved and waiting WeChat refund callback.', 1, 2, DATE_SUB(NOW(), INTERVAL 10 MINUTE), NULL, '', 'test_sp_platform', DATE_SUB(NOW(), INTERVAL 30 MINUTE), NOW(), '0'),
(996003, 'TESTSP9901007', 'TESTSPR9901007R', 9901, 991001, 995007, 88.88, 'Rejected refund smoke.', 1, 3, DATE_SUB(NOW(), INTERVAL 10 MINUTE), NULL, 'Coupon already used.', 'test_sp_platform', DATE_SUB(NOW(), INTERVAL 40 MINUTE), NOW(), '0'),
(996004, 'TESTSP9901005', 'TESTSPR9901005S', 9901, 991002, 995005, 19.90, 'Refund succeeded callback received.', 1, 4, DATE_SUB(NOW(), INTERVAL 4 DAY), DATE_SUB(NOW(), INTERVAL 4 DAY), '', 'wechat_notify', DATE_SUB(NOW(), INTERVAL 5 DAY), NOW(), '0'),
(996005, 'TESTSP9902004', 'TESTSPR9902004S', 9902, 991003, 995014, 19.90, 'Second merchant refund succeeded.', 1, 4, DATE_SUB(NOW(), INTERVAL 4 DAY), DATE_SUB(NOW(), INTERVAL 4 DAY), '', 'wechat_notify', DATE_SUB(NOW(), INTERVAL 5 DAY), NOW(), '0'),
(996006, 'TESTSP9902002', 'TESTSPR9902002X', 9902, 991003, 995012, 10.00, 'Refund abnormal smoke.', 1, 5, DATE_SUB(NOW(), INTERVAL 5 MINUTE), NULL, 'WECHAT_REFUND_ABNORMAL', 'wechat_notify', DATE_SUB(NOW(), INTERVAL 15 MINUTE), NOW(), '0')
ON DUPLICATE KEY UPDATE status = VALUES(status), refund_time = VALUES(refund_time), reject_reason = VALUES(reject_reason), update_time = NOW(), del_flag = '0';

INSERT INTO write_off_record
(id, order_id, order_no, write_off_code, merchant_id, store_id, operator_id, operator_name, write_off_type,
 write_off_time, product_name, product_amount, remark, status, create_time)
VALUES
(996501, 994003, 'TESTSP9901003', 'WO9901003', 9901, 991302, 991402, 'Staff 9901', 1, DATE_SUB(NOW(), INTERVAL 1 DAY), 'TESTSP 9901 Latte Set 9.99', 99.90, 'Used before refund approval.', 1, NOW()),
(996502, 994004, 'TESTSP9901004', 'WO9901004', 9901, 991301, 991401, 'Owner 9901', 1, DATE_SUB(NOW(), INTERVAL 3 DAY), 'TESTSP 9901 Family Set 199.99', 199.99, 'Completed order write-off.', 1, NOW()),
(996503, 994013, 'TESTSP9902003', 'WO9902003', 9902, 991303, 991403, 'Owner 9902', 1, DATE_SUB(NOW(), INTERVAL 2 DAY), 'TESTSP 9902 Cake Set 88.88', 88.88, 'Second merchant completed order write-off.', 1, NOW()),
(996504, 994021, 'TESTSP9905001', 'WO9905001', 9905, 991306, 991404, 'Owner 9905', 1, DATE_SUB(NOW(), INTERVAL 1 DAY), 'TESTSP 9905 Platform Item 68.01', 68.01, 'Platform merchant write-off.', 1, NOW())
ON DUPLICATE KEY UPDATE write_off_time = VALUES(write_off_time), status = VALUES(status);

INSERT INTO platform_income
(id, merchant_id, order_no, order_amount, commission_rate, commission, create_time, del_flag)
VALUES
(994401, 9901, 'TESTSP9901004', 199.99, 10.00, 20.00, NOW(), '0'),
(994402, 9902, 'TESTSP9902003', 88.88, 10.00, 8.89, NOW(), '0'),
(994403, 9905, 'TESTSP9905001', 68.01, 10.00, 6.80, NOW(), '0')
ON DUPLICATE KEY UPDATE order_amount = VALUES(order_amount), commission = VALUES(commission), del_flag = '0';

INSERT INTO transaction_record
(id, merchant_id, type, amount, balance, order_no, description, create_time, del_flag)
VALUES
(994501, 9901, 'payment', 199.99, 1400.00, 'TESTSP9901004', 'TEST service-provider payment success.', NOW(), '0'),
(994502, 9901, 'commission', -20.00, 1380.00, 'TESTSP9901004', 'TEST platform commission.', NOW(), '0'),
(994503, 9901, 'refund', -19.90, 1360.10, 'TESTSP9901005', 'TEST refund success.', NOW(), '0'),
(994504, 9902, 'payment', 88.88, 980.00, 'TESTSP9902003', 'TEST second merchant payment.', NOW(), '0'),
(994505, 9905, 'payment', 68.01, 320.00, 'TESTSP9905001', 'TEST platform-owned merchant payment.', NOW(), '0')
ON DUPLICATE KEY UPDATE amount = VALUES(amount), balance = VALUES(balance), del_flag = '0';

-- -------------------------------------------------------------------
-- Settlement and transfer.
-- Settlement status coverage:
-- WAITING_T1, TRANSFERRING, ARRIVED, FAILED, CANCELLED, REFUND_PROCESSING, REVERSED.
-- -------------------------------------------------------------------
INSERT INTO merchant_settlement_record
(id, settlement_no, merchant_id, distributor_id, store_id, order_no, title, order_amount,
 merchant_amount, platform_fee_amount, status, apply_time, expected_transfer_time, transfer_time,
 arrive_time, fail_reason, wechat_batch_no, wechat_detail_no, reverse_record_id, del_flag,
 create_time, update_time)
VALUES
(997001, 'TESTSPMS9901002', 9901, 9901, 991301, 'TESTSP9901002', 'Paid waiting T1', 9.99, 8.19, 1.00, 'WAITING_T1', DATE_SUB(NOW(), INTERVAL 3 HOUR), DATE_ADD(NOW(), INTERVAL 21 HOUR), NULL, NULL, NULL, NULL, NULL, NULL, '0', NOW(), NOW()),
(997002, 'TESTSPMS9901003', 9901, 9901, 991302, 'TESTSP9901003', 'Refund processing after approved refund', 99.90, 81.92, 9.99, 'REFUND_PROCESSING', DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 12 HOUR), NULL, NULL, 'BATCH_TEST_9901003', 'DETAIL_TEST_9901003', NULL, '0', NOW(), NOW()),
(997003, 'TESTSPMS9901004', 9901, 9901, 991301, 'TESTSP9901004', 'Completed arrived settlement', 199.99, 163.99, 20.00, 'ARRIVED', DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 22 HOUR), DATE_SUB(NOW(), INTERVAL 20 HOUR), NULL, 'BATCH_TEST_9901004', 'DETAIL_TEST_9901004', NULL, '0', NOW(), NOW()),
(997004, 'TESTSPMS9901005', 9901, 9901, 991301, 'TESTSP9901005', 'Cancelled by refund before T1', 19.90, 16.32, 1.99, 'CANCELLED', DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL 4 DAY), NULL, NULL, 'Refunded before transfer.', NULL, NULL, NULL, '0', NOW(), NOW()),
(997005, 'TESTSPMS9902002', 9902, 9901, 991303, 'TESTSP9902002', 'Transferring settlement', 80.88, 68.75, 8.09, 'TRANSFERRING', DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 2 HOUR), DATE_SUB(NOW(), INTERVAL 1 HOUR), NULL, NULL, 'BATCH_TEST_9902002', 'DETAIL_TEST_9902002', NULL, '0', NOW(), NOW()),
(997006, 'TESTSPMS9902003', 9902, 9901, 991303, 'TESTSP9902003', 'Failed settlement', 88.88, 75.55, 8.89, 'FAILED', DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY), NULL, 'Receiver account abnormal.', 'BATCH_TEST_9902003', 'DETAIL_TEST_9902003', NULL, '0', NOW(), NOW()),
(997007, 'TESTSPMS9905001', 9905, NULL, 991306, 'TESTSP9905001', 'Platform merchant arrived settlement', 68.01, 61.21, 6.80, 'ARRIVED', DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 23 HOUR), DATE_SUB(NOW(), INTERVAL 22 HOUR), NULL, 'BATCH_TEST_9905001', 'DETAIL_TEST_9905001', NULL, '0', NOW(), NOW()),
(997008, 'TESTSPMS9901004R', 9901, 9901, 991301, 'TESTSP9901004', 'Reverse record for arrived settlement', -199.99, -163.99, -20.00, 'REVERSED', DATE_SUB(NOW(), INTERVAL 10 HOUR), NULL, NULL, NULL, 'Reverse smoke record.', NULL, NULL, 997003, '0', NOW(), NOW())
ON DUPLICATE KEY UPDATE status = VALUES(status), merchant_amount = VALUES(merchant_amount), platform_fee_amount = VALUES(platform_fee_amount), update_time = NOW(), del_flag = '0';

INSERT INTO order_profit_ledger
(id, order_no, merchant_id, distributor_id, pay_amount, merchant_amount, platform_amount,
 distributor_amount, merchant_rate, platform_rate, distributor_rate, status, finish_time, del_flag,
 create_time, update_time)
VALUES
(998001, 'TESTSP9901002', 9901, 9901, 9.99, 8.19, 1.00, 0.80, 82.00, 10.00, 8.00, 'WAITING_SETTLEMENT', NULL, '0', NOW(), NOW()),
(998002, 'TESTSP9901003', 9901, 9901, 99.90, 81.92, 9.99, 7.99, 82.00, 10.00, 8.00, 'WAITING_SETTLEMENT', DATE_SUB(NOW(), INTERVAL 1 DAY), '0', NOW(), NOW()),
(998003, 'TESTSP9901004', 9901, 9901, 199.99, 163.99, 20.00, 16.00, 82.00, 10.00, 8.00, 'SETTLED', DATE_SUB(NOW(), INTERVAL 2 DAY), '0', NOW(), NOW()),
(998004, 'TESTSP9901005', 9901, 9901, 19.90, 16.32, 1.99, 1.59, 82.00, 10.00, 8.00, 'REFUND_REVERSED', DATE_SUB(NOW(), INTERVAL 4 DAY), '0', NOW(), NOW()),
(998011, 'TESTSP9902002', 9902, 9901, 80.88, 68.75, 8.09, 4.04, 85.00, 10.00, 5.00, 'WAITING_SETTLEMENT', NULL, '0', NOW(), NOW()),
(998012, 'TESTSP9902003', 9902, 9901, 88.88, 75.55, 8.89, 4.44, 85.00, 10.00, 5.00, 'SETTLED', DATE_SUB(NOW(), INTERVAL 1 DAY), '0', NOW(), NOW()),
(998021, 'TESTSP9905001', 9905, NULL, 68.01, 61.21, 6.80, 0.00, 90.00, 10.00, 0.00, 'SETTLED', DATE_SUB(NOW(), INTERVAL 12 HOUR), '0', NOW(), NOW())
ON DUPLICATE KEY UPDATE merchant_amount = VALUES(merchant_amount), platform_amount = VALUES(platform_amount), distributor_amount = VALUES(distributor_amount), status = VALUES(status), update_time = NOW(), del_flag = '0';

INSERT INTO distributor_settlement_record
(id, settlement_no, distributor_id, merchant_id, order_no, amount, rate, status,
 settlement_period_start, settlement_period_end, expected_transfer_time, transfer_time, arrive_time,
 fail_reason, reverse_record_id, del_flag, create_time, update_time)
VALUES
(999001, 'TESTSPDS9901002', 9901, 9901, 'TESTSP9901002', 0.80, 8.00, 'WAITING_SETTLEMENT', CURDATE(), CURDATE(), DATE_ADD(NOW(), INTERVAL 21 HOUR), NULL, NULL, NULL, NULL, '0', NOW(), NOW()),
(999002, 'TESTSPDS9901004', 9901, 9901, 'TESTSP9901004', 16.00, 8.00, 'ARRIVED', DATE_SUB(CURDATE(), INTERVAL 3 DAY), DATE_SUB(CURDATE(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 22 HOUR), DATE_SUB(NOW(), INTERVAL 20 HOUR), NULL, NULL, '0', NOW(), NOW()),
(999003, 'TESTSPDS9902002', 9901, 9902, 'TESTSP9902002', 4.04, 5.00, 'TRANSFERRING', CURDATE(), CURDATE(), DATE_SUB(NOW(), INTERVAL 2 HOUR), DATE_SUB(NOW(), INTERVAL 1 HOUR), NULL, NULL, NULL, '0', NOW(), NOW()),
(999004, 'TESTSPDS9902003', 9901, 9902, 'TESTSP9902003', 4.44, 5.00, 'FAILED', DATE_SUB(CURDATE(), INTERVAL 3 DAY), DATE_SUB(CURDATE(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY), NULL, 'Receiver account abnormal.', NULL, '0', NOW(), NOW()),
(999005, 'TESTSPDS9901005', 9901, 9901, 'TESTSP9901005', 1.59, 8.00, 'REVERSED', DATE_SUB(CURDATE(), INTERVAL 5 DAY), DATE_SUB(CURDATE(), INTERVAL 4 DAY), NULL, NULL, NULL, 'Refund reversed.', 999002, '0', NOW(), NOW())
ON DUPLICATE KEY UPDATE status = VALUES(status), amount = VALUES(amount), fail_reason = VALUES(fail_reason), update_time = NOW(), del_flag = '0';

INSERT INTO platform_transfer_record
(id, transfer_no, settlement_no, target_type, target_id, merchant_id, distributor_id, order_no, amount,
 receiver_openid, receiver_name, receiver_account_type, wechat_batch_no, wechat_detail_no, status,
 fail_reason, apply_time, transfer_time, arrive_time, notify_time, notify_result, operator_id, remark,
 del_flag, create_time, update_time)
VALUES
(999501, 'TESTSPTRM9901004', 'TESTSPMS9901004', 'MERCHANT', 9901, 9901, 9901, 'TESTSP9901004', 163.99, 'receiver_openid_mch_9901', 'TESTSP Merchant 9901', 'WECHAT_BALANCE', 'BATCH_TEST_9901004', 'DETAIL_TEST_9901004', 'ARRIVED', NULL, DATE_SUB(NOW(), INTERVAL 23 HOUR), DATE_SUB(NOW(), INTERVAL 22 HOUR), DATE_SUB(NOW(), INTERVAL 20 HOUR), DATE_SUB(NOW(), INTERVAL 20 HOUR), 'SUCCESS', '990001', 'Merchant arrived transfer.', '0', NOW(), NOW()),
(999502, 'TESTSPTRD9901004', 'TESTSPDS9901004', 'DISTRIBUTOR', 9901, 9901, 9901, 'TESTSP9901004', 16.00, 'receiver_openid_dist_9901', 'TESTSP Distributor North', 'WECHAT_BALANCE', 'BATCH_TEST_D9901004', 'DETAIL_TEST_D9901004', 'ARRIVED', NULL, DATE_SUB(NOW(), INTERVAL 23 HOUR), DATE_SUB(NOW(), INTERVAL 22 HOUR), DATE_SUB(NOW(), INTERVAL 20 HOUR), DATE_SUB(NOW(), INTERVAL 20 HOUR), 'SUCCESS', '990001', 'Distributor arrived transfer.', '0', NOW(), NOW()),
(999503, 'TESTSPTRM9902002', 'TESTSPMS9902002', 'MERCHANT', 9902, 9902, 9901, 'TESTSP9902002', 68.75, 'receiver_openid_mch_9902', 'TESTSP Merchant 9902', 'WECHAT_BALANCE', 'BATCH_TEST_9902002', 'DETAIL_TEST_9902002', 'TRANSFERRING', NULL, DATE_SUB(NOW(), INTERVAL 2 HOUR), DATE_SUB(NOW(), INTERVAL 1 HOUR), NULL, NULL, '', '990001', 'Merchant transferring transfer.', '0', NOW(), NOW()),
(999504, 'TESTSPTRM9902003', 'TESTSPMS9902003', 'MERCHANT', 9902, 9902, 9901, 'TESTSP9902003', 75.55, 'receiver_openid_mch_9902', 'TESTSP Merchant 9902', 'WECHAT_BALANCE', 'BATCH_TEST_9902003', 'DETAIL_TEST_9902003', 'FAILED', 'Receiver account abnormal.', DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY), NULL, DATE_SUB(NOW(), INTERVAL 2 DAY), 'FAILED', '990001', 'Failed transfer test.', '0', NOW(), NOW()),
(999505, 'TESTSPTRM9905001', 'TESTSPMS9905001', 'MERCHANT', 9905, 9905, NULL, 'TESTSP9905001', 61.21, 'receiver_openid_mch_9905', 'TESTSP Merchant 9905', 'WECHAT_BALANCE', 'BATCH_TEST_9905001', 'DETAIL_TEST_9905001', 'ARRIVED', NULL, DATE_SUB(NOW(), INTERVAL 23 HOUR), DATE_SUB(NOW(), INTERVAL 23 HOUR), DATE_SUB(NOW(), INTERVAL 22 HOUR), DATE_SUB(NOW(), INTERVAL 22 HOUR), 'SUCCESS', '990001', 'Platform-owned merchant transfer.', '0', NOW(), NOW())
ON DUPLICATE KEY UPDATE status = VALUES(status), fail_reason = VALUES(fail_reason), notify_result = VALUES(notify_result), update_time = NOW(), del_flag = '0';

COMMIT;

-- -------------------------------------------------------------------
-- Quick smoke queries after seed.
-- -------------------------------------------------------------------
SELECT 'merchants' AS item, COUNT(*) AS cnt FROM merchant WHERE id BETWEEN 9901 AND 9909
UNION ALL SELECT 'orders', COUNT(*) FROM mall_order WHERE order_no LIKE 'TESTSP%'
UNION ALL SELECT 'payments', COUNT(*) FROM payment_record WHERE order_no LIKE 'TESTSP%'
UNION ALL SELECT 'refunds', COUNT(*) FROM refund_record WHERE refund_no LIKE 'TESTSPR%'
UNION ALL SELECT 'merchant_settlements', COUNT(*) FROM merchant_settlement_record WHERE settlement_no LIKE 'TESTSPMS%'
UNION ALL SELECT 'transfers', COUNT(*) FROM platform_transfer_record WHERE transfer_no LIKE 'TESTSPTR%';

SELECT id, name, c_mini_app_id, merchant_wx_mch_id, wx_profit_sharing_enabled,
       merchant_share_rate, platform_share_rate, distributor_share_rate, status
FROM merchant
WHERE id BETWEEN 9901 AND 9905
ORDER BY id;
