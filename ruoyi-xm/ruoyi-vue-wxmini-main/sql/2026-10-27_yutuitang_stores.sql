-- ============================================================
-- 裕推堂（merchant_id = 5）上线基础数据
-- 数据来源：微信小程序后台「微信位置」列表（2026-10-27）+ 商家提供经纬度
-- 说明：
--   1. AppID 更正为 wxdd01ea360a695123（原 wxdba77161eb5ad3be 是错的）
--   2. AppSecret 不在此脚本内覆盖，沿用线上已有值（换 AppID 后需确认与之一致）
--   3. 现有 id=5 占位门店升级为北长安街店并设为主门店；其余 5 家幂等插入
--   4. 经纬度为 GCJ-02（与微信 openLocation 同坐标系）
-- ============================================================
USE `ruoyi-cs`;

-- 0) 更正小程序 AppID（不覆盖 AppSecret）
UPDATE merchant
SET c_mini_app_id = 'wxdd01ea360a695123',
    update_time = NOW()
WHERE id = 5;

-- 1) 占位门店升级为北长安街店（主门店）
UPDATE merchant_store
SET name = '裕推堂(北长安街店)',
    address = '陕西省西安市长安区北吉安小区东门口综合楼一单元二楼',
    phone = '18092742914',
    business_hours = '10:00-21:00',
    longitude = 108.944988,
    latitude = 34.166954,
    is_main = 1,
    status = 1,
    update_time = NOW()
WHERE merchant_id = 5 AND name = '裕推堂' AND del_flag = '0';

-- 2) 其余门店（幂等）
INSERT INTO merchant_store (merchant_id, name, phone, address, business_hours, longitude, latitude, status, is_main, del_flag, create_time, update_time)
SELECT 5, '裕推堂(九如御小区东区店)', '18392131135',
       '陕西省西安市未央区徐家湾街道经济技术开发区九如御小区6号楼一层6-102商铺',
       '09:00-20:00', 108.954682, 34.351475, 1, 0, '0', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM merchant_store WHERE merchant_id = 5 AND name = '裕推堂(九如御小区东区店)' AND del_flag = '0');

INSERT INTO merchant_store (merchant_id, name, phone, address, business_hours, longitude, latitude, status, is_main, del_flag, create_time, update_time)
SELECT 5, '裕推堂甘家寨', '18392526080',
       '陕西省西安市雁塔区甘家寨东2排31号',
       '09:00-21:00', 108.901262, 34.216382, 1, 0, '0', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM merchant_store WHERE merchant_id = 5 AND name = '裕推堂甘家寨' AND del_flag = '0');

INSERT INTO merchant_store (merchant_id, name, phone, address, business_hours, longitude, latitude, status, is_main, del_flag, create_time, update_time)
SELECT 5, '裕推堂五路口奇草御康', '15686926891',
       '陕西省西安市新城区裕朗国际12楼1259',
       '10:00-20:00', 108.964883, 34.265747, 1, 0, '0', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM merchant_store WHERE merchant_id = 5 AND name = '裕推堂五路口奇草御康' AND del_flag = '0');

INSERT INTO merchant_store (merchant_id, name, phone, address, business_hours, longitude, latitude, status, is_main, del_flag, create_time, update_time)
SELECT 5, '裕推堂筋骨调理馆.爱学路店30街坊北门店', '15229331811',
       '陕西省西安市新城区爱学路30街坊北门',
       '09:00-19:00', 109.007731, 34.253280, 1, 0, '0', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM merchant_store WHERE merchant_id = 5 AND name = '裕推堂筋骨调理馆.爱学路店30街坊北门店' AND del_flag = '0');

INSERT INTO merchant_store (merchant_id, name, phone, address, business_hours, longitude, latitude, status, is_main, del_flag, create_time, update_time)
SELECT 5, '裕推堂(富力城店)', '15902902467',
       '陕西省西安市长安区富力城北区7号楼1单元103',
       '10:00-20:00', 108.975487, 34.168143, 1, 0, '0', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM merchant_store WHERE merchant_id = 5 AND name = '裕推堂(富力城店)' AND del_flag = '0');

-- 3) 只保留一个主门店
UPDATE merchant_store
SET is_main = 0
WHERE merchant_id = 5 AND del_flag = '0' AND name <> '裕推堂(北长安街店)' AND is_main = 1;

-- 4) 回填商家门店数
UPDATE merchant
SET store_count = (SELECT COUNT(1) FROM merchant_store WHERE merchant_id = 5 AND del_flag = '0')
WHERE id = 5;

-- 5) 校验
-- SELECT id, name, phone, address, business_hours, longitude, latitude, is_main
-- FROM merchant_store WHERE merchant_id = 5 AND del_flag = '0' ORDER BY is_main DESC, id;
