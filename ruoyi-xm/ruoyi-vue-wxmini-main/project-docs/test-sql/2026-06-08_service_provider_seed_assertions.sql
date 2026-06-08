-- 2026-06-08 service-provider seed assertions.
-- Run after project-docs/test-sql/2026-06-08_service_provider_full_test_seed.sql.
-- The result set should be reviewed before release tests. Any FAIL means the seed or schema is not ready.

SET NAMES utf8mb4;

SELECT 'A01 merchants count' AS check_item,
       CASE WHEN COUNT(*) = 5 THEN 'PASS' ELSE CONCAT('FAIL cnt=', COUNT(*), ', expected=5') END AS result
FROM merchant
WHERE id BETWEEN 9901 AND 9905 AND del_flag = '0'
UNION ALL
SELECT 'A02 distributors count',
       CASE WHEN COUNT(*) = 3 THEN 'PASS' ELSE CONCAT('FAIL cnt=', COUNT(*), ', expected=3') END
FROM distributor
WHERE id BETWEEN 9901 AND 9903
UNION ALL
SELECT 'A03 products count',
       CASE WHEN COUNT(*) = 9 THEN 'PASS' ELSE CONCAT('FAIL cnt=', COUNT(*), ', expected=9') END
FROM product
WHERE id BETWEEN 993001 AND 993099 AND del_flag = '0'
UNION ALL
SELECT 'A04 orders count',
       CASE WHEN COUNT(*) = 13 THEN 'PASS' ELSE CONCAT('FAIL cnt=', COUNT(*), ', expected=13') END
FROM mall_order
WHERE order_no LIKE 'TESTSP%' AND del_flag = '0'
UNION ALL
SELECT 'A05 payments count',
       CASE WHEN COUNT(*) = 13 THEN 'PASS' ELSE CONCAT('FAIL cnt=', COUNT(*), ', expected=13') END
FROM payment_record
WHERE order_no LIKE 'TESTSP%' AND del_flag = '0'
UNION ALL
SELECT 'A06 refunds count',
       CASE WHEN COUNT(*) = 6 THEN 'PASS' ELSE CONCAT('FAIL cnt=', COUNT(*), ', expected=6') END
FROM refund_record
WHERE refund_no LIKE 'TESTSPR%' AND del_flag = '0'
UNION ALL
SELECT 'A07 merchant settlements count',
       CASE WHEN COUNT(*) = 8 THEN 'PASS' ELSE CONCAT('FAIL cnt=', COUNT(*), ', expected=8') END
FROM merchant_settlement_record
WHERE settlement_no LIKE 'TESTSPMS%' AND del_flag = '0'
UNION ALL
SELECT 'A08 distributor settlements count',
       CASE WHEN COUNT(*) = 5 THEN 'PASS' ELSE CONCAT('FAIL cnt=', COUNT(*), ', expected=5') END
FROM distributor_settlement_record
WHERE settlement_no LIKE 'TESTSPDS%' AND del_flag = '0'
UNION ALL
SELECT 'A09 transfers count',
       CASE WHEN COUNT(*) = 5 THEN 'PASS' ELSE CONCAT('FAIL cnt=', COUNT(*), ', expected=5') END
FROM platform_transfer_record
WHERE transfer_no LIKE 'TESTSPTR%' AND del_flag = '0';

SELECT 'B01 normal merchants have AppID and sub_mchid' AS check_item,
       CASE WHEN COUNT(*) = 0 THEN 'PASS' ELSE CONCAT('FAIL bad_rows=', COUNT(*)) END AS result
FROM merchant
WHERE id IN (9901, 9902, 9905)
  AND (
    c_mini_app_id IS NULL OR c_mini_app_id = ''
    OR c_mini_app_secret IS NULL OR c_mini_app_secret = ''
    OR merchant_wx_mch_id IS NULL OR merchant_wx_mch_id = ''
    OR wx_profit_sharing_enabled <> 1
  )
UNION ALL
SELECT 'B02 negative merchant 9903 is missing config',
       CASE WHEN COUNT(*) = 1 THEN 'PASS' ELSE CONCAT('FAIL cnt=', COUNT(*), ', expected=1') END
FROM merchant
WHERE id = 9903 AND c_mini_app_id = 'wx_test_sp_mch_9903'
  AND c_mini_app_secret IS NULL AND merchant_wx_mch_id IS NULL
UNION ALL
SELECT 'B03 negative merchant 9904 is stopped',
       CASE WHEN COUNT(*) = 1 THEN 'PASS' ELSE CONCAT('FAIL cnt=', COUNT(*), ', expected=1') END
FROM merchant
WHERE id = 9904 AND status = 3
UNION ALL
SELECT 'B04 AppID unique in seed',
       CASE WHEN COUNT(*) = 0 THEN 'PASS' ELSE CONCAT('FAIL duplicate_appids=', COUNT(*)) END
FROM (
    SELECT c_mini_app_id
    FROM merchant
    WHERE id BETWEEN 9901 AND 9905 AND c_mini_app_id IS NOT NULL AND c_mini_app_id <> ''
    GROUP BY c_mini_app_id
    HAVING COUNT(*) > 1
) d
UNION ALL
SELECT 'B05 share rates sum to 100',
       CASE WHEN COUNT(*) = 0 THEN 'PASS' ELSE CONCAT('FAIL bad_rows=', COUNT(*)) END
FROM merchant
WHERE id IN (9901, 9902, 9905)
  AND ROUND(IFNULL(merchant_share_rate, 0) + IFNULL(platform_share_rate, 0) + IFNULL(distributor_share_rate, 0), 2) <> 100.00;

SELECT 'C01 seeded order users exist in user_info' AS check_item,
       CASE WHEN COUNT(*) = 0 THEN 'PASS' ELSE CONCAT('FAIL bad_rows=', COUNT(*)) END AS result
FROM mall_order o
LEFT JOIN user_info u ON u.id = o.user_id
WHERE o.order_no LIKE 'TESTSP%' AND u.id IS NULL
UNION ALL
SELECT 'C02 payment users match orders',
       CASE WHEN COUNT(*) = 0 THEN 'PASS' ELSE CONCAT('FAIL bad_rows=', COUNT(*)) END
FROM payment_record p
JOIN mall_order o ON o.order_no = p.order_no
WHERE p.order_no LIKE 'TESTSP%' AND p.user_id <> o.user_id
UNION ALL
SELECT 'C03 refund users match orders',
       CASE WHEN COUNT(*) = 0 THEN 'PASS' ELSE CONCAT('FAIL bad_rows=', COUNT(*)) END
FROM refund_record r
JOIN mall_order o ON o.order_no = r.order_no
WHERE r.refund_no LIKE 'TESTSPR%' AND r.user_id <> o.user_id
UNION ALL
SELECT 'C04 user_info and mall_user open_id mirrored',
       CASE WHEN COUNT(*) = 0 THEN 'PASS' ELSE CONCAT('FAIL bad_rows=', COUNT(*)) END
FROM user_info u
LEFT JOIN mall_user mu ON mu.id = u.id AND mu.open_id = u.open_id
WHERE u.id BETWEEN 991001 AND 991005 AND mu.id IS NULL;

SELECT 'D01 product prices are exact cents' AS check_item,
       CASE WHEN COUNT(*) = 0 THEN 'PASS' ELSE CONCAT('FAIL bad_rows=', COUNT(*)) END AS result
FROM product
WHERE id BETWEEN 993001 AND 993099
  AND price IS NOT NULL
  AND ROUND(price * 100, 0) <> price * 100
UNION ALL
SELECT 'D02 order pay amounts are exact cents',
       CASE WHEN COUNT(*) = 0 THEN 'PASS' ELSE CONCAT('FAIL bad_rows=', COUNT(*)) END
FROM mall_order
WHERE order_no LIKE 'TESTSP%'
  AND pay_amount IS NOT NULL
  AND ROUND(pay_amount * 100, 0) <> pay_amount * 100
UNION ALL
SELECT 'D03 payment amount equals order pay amount',
       CASE WHEN COUNT(*) = 0 THEN 'PASS' ELSE CONCAT('FAIL bad_rows=', COUNT(*)) END
FROM payment_record p
JOIN mall_order o ON o.order_no = p.order_no
WHERE p.order_no LIKE 'TESTSP%'
  AND ROUND(p.amount, 2) <> ROUND(o.pay_amount, 2)
UNION ALL
SELECT 'D04 profit ledger split equals pay amount',
       CASE WHEN COUNT(*) = 0 THEN 'PASS' ELSE CONCAT('FAIL bad_rows=', COUNT(*)) END
FROM order_profit_ledger l
WHERE l.order_no LIKE 'TESTSP%'
  AND ROUND(IFNULL(l.merchant_amount, 0) + IFNULL(l.platform_amount, 0) + IFNULL(l.distributor_amount, 0), 2)
      <> ROUND(IFNULL(l.pay_amount, 0), 2)
UNION ALL
SELECT 'D05 platform merchant has no distributor share',
       CASE WHEN COUNT(*) = 1 THEN 'PASS' ELSE CONCAT('FAIL cnt=', COUNT(*), ', expected=1') END
FROM order_profit_ledger
WHERE order_no = 'TESTSP9905001' AND distributor_id IS NULL AND distributor_amount = 0.00;

SELECT 'E01 transfer rows have receiver and target' AS check_item,
       CASE WHEN COUNT(*) = 0 THEN 'PASS' ELSE CONCAT('FAIL bad_rows=', COUNT(*)) END AS result
FROM platform_transfer_record
WHERE transfer_no LIKE 'TESTSPTR%'
  AND (
    target_type NOT IN ('MERCHANT', 'DISTRIBUTOR')
    OR receiver_openid IS NULL OR receiver_openid = ''
    OR amount <= 0
  )
UNION ALL
SELECT 'E02 transfer settlement refs exist',
       CASE WHEN COUNT(*) = 0 THEN 'PASS' ELSE CONCAT('FAIL bad_rows=', COUNT(*)) END
FROM platform_transfer_record t
LEFT JOIN merchant_settlement_record ms
       ON t.target_type = 'MERCHANT' AND ms.settlement_no = t.settlement_no
LEFT JOIN distributor_settlement_record ds
       ON t.target_type = 'DISTRIBUTOR' AND ds.settlement_no = t.settlement_no
WHERE t.transfer_no LIKE 'TESTSPTR%'
  AND (
    (t.target_type = 'MERCHANT' AND ms.id IS NULL)
    OR (t.target_type = 'DISTRIBUTOR' AND ds.id IS NULL)
  )
UNION ALL
SELECT 'E03 backend test users exist',
       CASE WHEN COUNT(*) = 5 THEN 'PASS' ELSE CONCAT('FAIL cnt=', COUNT(*), ', expected=5') END
FROM sys_user
WHERE user_name IN ('test_sp_platform', 'test_sp_dist_9901', 'test_sp_dist_9902', 'test_sp_mch_9901', 'test_sp_mch_9902')
  AND del_flag = '0';

SELECT 'Seed key accounts' AS info, user_id, user_name, account_type, distributor_id, merchant_id
FROM sys_user
WHERE user_name LIKE 'test_sp_%'
ORDER BY user_id;

SELECT 'Seed miniapp config' AS info, id, name, status, c_mini_app_id, c_mini_app_secret,
       merchant_wx_mch_id, merchant_share_rate, platform_share_rate, distributor_share_rate
FROM merchant
WHERE id BETWEEN 9901 AND 9905
ORDER BY id;
