-- ============================================================
-- 裕推堂：由“调理”商品切换为“生活美容/丽人服务”审核内容
-- 旧商品仅下架（status=0），不删除数据，随时可恢复
-- ============================================================
USE `ruoyi-cs`;

-- 1) 下架旧调理商品及其预点单服务
UPDATE product
SET status = 0, update_time = NOW()
WHERE merchant_id = 5 AND del_flag = '0'
  AND name IN ('肩颈深度调理（60分钟）', '腰背舒缓调理（60分钟）', '全身经络推拿（90分钟）',
               '古法艾灸调理（60分钟）', '拔罐祛湿调理（40分钟）', '刮痧排毒调理（40分钟）');

UPDATE mall_booking_service
SET status = 0, update_time = NOW()
WHERE merchant_id = 5
  AND product_id IN (
      SELECT id FROM (
          SELECT id FROM product
          WHERE merchant_id = 5 AND del_flag = '0' AND status = 0
            AND name IN ('肩颈深度调理（60分钟）', '腰背舒缓调理（60分钟）', '全身经络推拿（90分钟）',
                         '古法艾灸调理（60分钟）', '拔罐祛湿调理（40分钟）', '刮痧排毒调理（40分钟）')
      ) t
  );

UPDATE product_category
SET status = 0, update_time = NOW()
WHERE merchant_id = 5 AND name = '调理服务';

-- 2) 新建丽人服务分类
INSERT INTO product_category (merchant_id, name, sort, status, del_flag, create_time, update_time)
SELECT 5, '丽人服务', 0, 1, '0', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM product_category WHERE merchant_id = 5 AND name = '丽人服务');

SET @cat := (SELECT id FROM product_category WHERE merchant_id = 5 AND name = '丽人服务' LIMIT 1);

-- 3) 上架 6 个生活美容商品
INSERT INTO product (merchant_id, category_id, name, cover_image, main_image, images, price, original_price,
                     stock, sales, status, verify_type, valid_days, verify_notice, description, store_ids, sort,
                     create_time, update_time)
SELECT 5, @cat, '单色美甲（60分钟）',
       '/profile/merchant_images/5/product/temp/main_beauty_1.png',
       '/profile/merchant_images/5/product/temp/main_beauty_1.png', '',
       59.00, 98.00, 200, 0, 1, 1, 365,
       '六店通用 · 建议提前2小时预约',
       '含基础修护与单色甲油胶，六店通用，到店出示核销码即可使用。',
       '', 1, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM product WHERE merchant_id = 5 AND name = '单色美甲（60分钟）' AND del_flag = '0');

INSERT INTO product (merchant_id, category_id, name, cover_image, main_image, images, price, original_price,
                     stock, sales, status, verify_type, valid_days, verify_notice, description, store_ids, sort,
                     create_time, update_time)
SELECT 5, @cat, '日式美睫（90分钟）',
       '/profile/merchant_images/5/product/temp/main_beauty_2.png',
       '/profile/merchant_images/5/product/temp/main_beauty_2.png', '',
       99.00, 168.00, 200, 0, 1, 1, 365,
       '六店通用 · 建议提前2小时预约',
       '自然轻盈款美睫，六店通用，到店出示核销码即可使用。',
       '', 2, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM product WHERE merchant_id = 5 AND name = '日式美睫（90分钟）' AND del_flag = '0');

INSERT INTO product (merchant_id, category_id, name, cover_image, main_image, images, price, original_price,
                     stock, sales, status, verify_type, valid_days, verify_notice, description, store_ids, sort,
                     create_time, update_time)
SELECT 5, @cat, '面部补水护理（60分钟）',
       '/profile/merchant_images/5/product/temp/main_beauty_3.png',
       '/profile/merchant_images/5/product/temp/main_beauty_3.png', '',
       128.00, 198.00, 200, 0, 1, 1, 365,
       '六店通用 · 建议提前2小时预约',
       '深层补水面部护理，六店通用，到店出示核销码即可使用。',
       '', 3, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM product WHERE merchant_id = 5 AND name = '面部补水护理（60分钟）' AND del_flag = '0');

INSERT INTO product (merchant_id, category_id, name, cover_image, main_image, images, price, original_price,
                     stock, sales, status, verify_type, valid_days, verify_notice, description, store_ids, sort,
                     create_time, update_time)
SELECT 5, @cat, '深层清洁护理（60分钟）',
       '/profile/merchant_images/5/product/temp/main_beauty_4.png',
       '/profile/merchant_images/5/product/temp/main_beauty_4.png', '',
       99.00, 158.00, 200, 0, 1, 1, 365,
       '六店通用 · 建议提前2小时预约',
       '毛孔净透深层清洁，六店通用，到店出示核销码即可使用。',
       '', 4, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM product WHERE merchant_id = 5 AND name = '深层清洁护理（60分钟）' AND del_flag = '0');

INSERT INTO product (merchant_id, category_id, name, cover_image, main_image, images, price, original_price,
                     stock, sales, status, verify_type, valid_days, verify_notice, description, store_ids, sort,
                     create_time, update_time)
SELECT 5, @cat, '美甲款式设计（90分钟）',
       '/profile/merchant_images/5/product/temp/main_beauty_5.png',
       '/profile/merchant_images/5/product/temp/main_beauty_5.png', '',
       129.00, 199.00, 200, 0, 1, 1, 365,
       '六店通用 · 建议提前2小时预约',
       '含钻饰/贴片款式设计，六店通用，到店出示核销码即可使用。',
       '', 5, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM product WHERE merchant_id = 5 AND name = '美甲款式设计（90分钟）' AND del_flag = '0');

INSERT INTO product (merchant_id, category_id, name, cover_image, main_image, images, price, original_price,
                     stock, sales, status, verify_type, valid_days, verify_notice, description, store_ids, sort,
                     create_time, update_time)
SELECT 5, @cat, '手部嫩肤护理（40分钟）',
       '/profile/merchant_images/5/product/temp/main_beauty_6.png',
       '/profile/merchant_images/5/product/temp/main_beauty_6.png', '',
       49.00, 88.00, 200, 0, 1, 1, 365,
       '六店通用 · 建议提前2小时预约',
       '手部滋润养护，六店通用，到店出示核销码即可使用。',
       '', 6, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM product WHERE merchant_id = 5 AND name = '手部嫩肤护理（40分钟）' AND del_flag = '0');

-- 4) 商家简介、轮播与商品数
UPDATE merchant
SET description = '裕推堂生活美容，六店通用，提供美甲美睫、面部护理、手部护理等到店服务。',
    carousel_images = '/profile/merchant_images/5/product/temp/banner.png,/profile/merchant_images/5/product/temp/main_beauty_1.png,/profile/merchant_images/5/product/temp/main_beauty_3.png',
    product_count = (SELECT COUNT(1) FROM product WHERE merchant_id = 5 AND del_flag = '0' AND status = 1),
    update_time = NOW()
WHERE id = 5;

SELECT id, name, price, original_price, status, sort FROM product
WHERE merchant_id = 5 AND del_flag = '0' AND status = 1 ORDER BY sort;
