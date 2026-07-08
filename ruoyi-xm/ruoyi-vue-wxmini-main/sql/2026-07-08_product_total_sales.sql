-- Rebuild product cumulative sales from paid order items.
-- Counts orders that have paid successfully at least once:
-- 1=paid, 2=used, 3=completed, 4=refunded.

UPDATE product p
LEFT JOIN (
  SELECT
    oi.product_id,
    SUM(COALESCE(oi.quantity, 0)) AS total_quantity
  FROM order_item oi
  INNER JOIN mall_order o
    ON o.order_no = oi.order_no
   AND o.del_flag = '0'
   AND o.status IN (1, 2, 3, 4)
  WHERE oi.del_flag = '0'
  GROUP BY oi.product_id
) s ON s.product_id = p.id
SET p.sales = COALESCE(s.total_quantity, 0),
    p.update_time = NOW()
WHERE p.del_flag = '0';
