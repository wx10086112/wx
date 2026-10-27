-- ============================================================
-- 商家员工绑定门店（多门店）
-- owner 的 store_id 可为 NULL（代表全部门店）；member 必须绑定门店
-- ============================================================
USE `ruoyi-cs`;

ALTER TABLE merchant_user
    ADD COLUMN store_id BIGINT DEFAULT NULL COMMENT '所属门店ID（NULL=全部门店，仅owner）' AFTER role;

ALTER TABLE merchant_user
    ADD KEY idx_store_id (store_id);

-- 历史数据：owner 保持 NULL（全部门店）；member 若为空，暂按主门店回填，后续可在员工管理里调整
UPDATE merchant_user mu
JOIN (
    SELECT merchant_id, MIN(id) AS main_store_id
    FROM merchant_store
    WHERE del_flag = '0' AND status = 1
    GROUP BY merchant_id
) s ON s.merchant_id = mu.merchant_id
SET mu.store_id = s.main_store_id
WHERE mu.role <> 'owner' AND mu.store_id IS NULL AND mu.del_flag = '0';
