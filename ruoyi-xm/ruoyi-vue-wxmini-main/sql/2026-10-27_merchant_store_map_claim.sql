-- ============================================================
-- 腾讯地图认领下沉到门店级（多门店）
-- 原 merchant.map_claim_* 字段保留兼容，主门店先回填一份
-- ============================================================
USE `ruoyi-cs`;

ALTER TABLE merchant_store
    ADD COLUMN map_claim_status VARCHAR(32) DEFAULT 'NOT_CLAIMED' COMMENT '腾讯地图认领状态' AFTER is_main,
    ADD COLUMN map_poi_id VARCHAR(128) DEFAULT NULL COMMENT '腾讯地图POI ID' AFTER map_claim_status,
    ADD COLUMN map_claim_url VARCHAR(500) DEFAULT NULL COMMENT '腾讯地图认领或门店链接' AFTER map_poi_id,
    ADD COLUMN map_claim_time DATETIME DEFAULT NULL COMMENT '认领时间' AFTER map_claim_url,
    ADD COLUMN map_claim_remark VARCHAR(500) DEFAULT NULL COMMENT '认领备注' AFTER map_claim_time;

UPDATE merchant_store s
JOIN merchant m ON m.id = s.merchant_id
SET s.map_claim_status = m.map_claim_status,
    s.map_poi_id = m.map_poi_id,
    s.map_claim_url = m.map_claim_url,
    s.map_claim_time = m.map_claim_time,
    s.map_claim_remark = m.map_claim_remark
WHERE s.is_main = 1 AND s.del_flag = '0';

SELECT s.id, s.name, s.map_claim_status, s.map_poi_id
FROM merchant_store s
WHERE s.merchant_id = 5 AND s.del_flag = '0'
ORDER BY s.is_main DESC, s.id;
