-- Keep the cached merchant.store_count value aligned with active store rows.
-- This statement is idempotent and does not create or delete stores.
UPDATE merchant m
SET m.store_count = (
    SELECT COUNT(1)
    FROM merchant_store ms
    WHERE ms.merchant_id = m.id
      AND ms.del_flag = '0'
)
WHERE m.del_flag = '0';
