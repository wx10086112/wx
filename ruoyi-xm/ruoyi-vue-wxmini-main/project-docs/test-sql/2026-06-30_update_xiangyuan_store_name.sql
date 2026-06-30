-- Update Xiangyuan mini-program storefront name.
-- Target storefront: 湘缘食尚餐厅(梨园路店)

SET @target_store_name := '湘缘食尚餐厅(梨园路店)';

UPDATE merchant
SET name = @target_store_name,
    update_time = NOW()
WHERE del_flag = '0'
  AND (
    c_mini_app_id = 'wx3787e4242ea6027e'
    OR m_mini_app_id = 'wx3787e4242ea6027e'
    OR merchant_wx_mch_id = '1113814461'
    OR wx_pay_mch_id = '1113814461'
    OR name LIKE '湘缘食尚餐厅%'
    OR name LIKE '湘缘食尚%'
  );

UPDATE merchant_store ms
JOIN merchant m ON m.id = ms.merchant_id
SET ms.name = @target_store_name,
    ms.update_time = NOW()
WHERE ms.del_flag = '0'
  AND m.del_flag = '0'
  AND (
    m.c_mini_app_id = 'wx3787e4242ea6027e'
    OR m.m_mini_app_id = 'wx3787e4242ea6027e'
    OR m.merchant_wx_mch_id = '1113814461'
    OR m.wx_pay_mch_id = '1113814461'
    OR m.name = @target_store_name
  )
  AND (ms.is_main = 1 OR ms.name LIKE '湘缘食尚餐厅%' OR ms.name LIKE '湘缘食尚%');

SELECT id, name, c_mini_app_id, merchant_wx_mch_id
FROM merchant
WHERE del_flag = '0'
  AND name = @target_store_name;

SELECT ms.id, ms.merchant_id, ms.name, ms.is_main
FROM merchant_store ms
JOIN merchant m ON m.id = ms.merchant_id
WHERE ms.del_flag = '0'
  AND m.del_flag = '0'
  AND m.name = @target_store_name;
