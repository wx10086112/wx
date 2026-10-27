-- ============================================================
-- 裕推堂：审核通过后恢复“筋骨调理”商品内容
-- 丽人商品仅下架，不删除，后续仍可切回
-- ============================================================
USE `ruoyi-cs`;

-- 1) 恢复原调理商品及预点单服务
UPDATE product
SET status = 1, update_time = NOW()
WHERE merchant_id = 5 AND del_flag = '0'
  AND name IN ('肩颈深度调理（60分钟）', '腰背舒缓调理（60分钟）', '全身经络推拿（90分钟）',
               '古法艾灸调理（60分钟）', '拔罐祛湿调理（40分钟）', '刮痧排毒调理（40分钟）');

UPDATE mall_booking_service
SET status = 1, update_time = NOW()
WHERE merchant_id = 5
  AND product_id IN (
      SELECT id FROM (
          SELECT id FROM product
          WHERE merchant_id = 5 AND del_flag = '0'
            AND name IN ('肩颈深度调理（60分钟）', '腰背舒缓调理（60分钟）', '全身经络推拿（90分钟）',
                         '古法艾灸调理（60分钟）', '拔罐祛湿调理（40分钟）', '刮痧排毒调理（40分钟）')
      ) t
  );

-- 2) 下架丽人商品及其服务
UPDATE product
SET status = 0, update_time = NOW()
WHERE merchant_id = 5 AND del_flag = '0'
  AND name IN ('单色美甲（60分钟）', '日式美睫（90分钟）', '面部补水护理（60分钟）',
               '深层清洁护理（60分钟）', '美甲款式设计（90分钟）', '手部嫩肤护理（40分钟）');

UPDATE mall_booking_service
SET status = 0, update_time = NOW()
WHERE merchant_id = 5
  AND product_id IN (
      SELECT id FROM (
          SELECT id FROM product
          WHERE merchant_id = 5 AND del_flag = '0'
            AND name IN ('单色美甲（60分钟）', '日式美睫（90分钟）', '面部补水护理（60分钟）',
                         '深层清洁护理（60分钟）', '美甲款式设计（90分钟）', '手部嫩肤护理（40分钟）')
      ) t
  );

-- 3) 分类状态
UPDATE product_category SET status = 1, update_time = NOW() WHERE merchant_id = 5 AND name = '调理服务';
UPDATE product_category SET status = 0, update_time = NOW() WHERE merchant_id = 5 AND name = '丽人服务';

-- 4) 商家简介、轮播、商品数与品牌副标题
UPDATE merchant
SET description = '裕推堂筋骨调理，六店通用，专业调理师一对一服务。',
    carousel_images = '/profile/merchant_images/5/product/temp/banner_tuina.png,/profile/merchant_images/5/product/temp/main_ytt_1.png,/profile/merchant_images/5/product/temp/main_ytt_3.png',
    product_count = (SELECT COUNT(1) FROM product WHERE merchant_id = 5 AND del_flag = '0' AND status = 1),
    update_time = NOW()
WHERE id = 5;

UPDATE sys_config
SET config_value = '筋骨调理 · 六店通用', update_time = NOW()
WHERE config_key = 'mall.brand.slogan.5';

SELECT id, name, price, status, sort FROM product
WHERE merchant_id = 5 AND del_flag = '0' AND status = 1 ORDER BY sort;
