-- Product price synchronization integrity migration.
-- Run this after 20260717_booking_service.sql. It is safe to rerun.

-- Review these result sets before applying the constraints. Non-zero rows stop the migration.
SELECT 'booking service references a missing product' AS check_item,
       s.id AS booking_service_id, s.merchant_id, s.product_id
FROM mall_booking_service s
LEFT JOIN product p ON p.id = s.product_id
WHERE s.product_id IS NOT NULL
  AND p.id IS NULL;

SELECT 'booking service merchant differs from product merchant' AS check_item,
       s.id AS booking_service_id, s.merchant_id AS service_merchant_id,
       s.product_id, p.merchant_id AS product_merchant_id
FROM mall_booking_service s
JOIN product p ON p.id = s.product_id
WHERE s.product_id IS NOT NULL
  AND s.merchant_id <> p.merchant_id;

SELECT 'booking record references a missing booking service' AS check_item,
       b.id AS booking_id, b.booking_no, b.booking_service_id
FROM mall_booking b
LEFT JOIN mall_booking_service s ON s.id = b.booking_service_id
WHERE b.booking_service_id IS NOT NULL
  AND s.id IS NULL;

DELIMITER $$

DROP PROCEDURE IF EXISTS enforce_product_price_sync_constraints $$
CREATE PROCEDURE enforce_product_price_sync_constraints()
BEGIN
    DECLARE v_missing_product BIGINT DEFAULT 0;
    DECLARE v_cross_merchant BIGINT DEFAULT 0;
    DECLARE v_missing_service BIGINT DEFAULT 0;

    SELECT COUNT(*) INTO v_missing_product
    FROM mall_booking_service s
    LEFT JOIN product p ON p.id = s.product_id
    WHERE s.product_id IS NOT NULL AND p.id IS NULL;

    SELECT COUNT(*) INTO v_cross_merchant
    FROM mall_booking_service s
    JOIN product p ON p.id = s.product_id
    WHERE s.product_id IS NOT NULL AND s.merchant_id <> p.merchant_id;

    SELECT COUNT(*) INTO v_missing_service
    FROM mall_booking b
    LEFT JOIN mall_booking_service s ON s.id = b.booking_service_id
    WHERE b.booking_service_id IS NOT NULL AND s.id IS NULL;

    IF v_missing_product > 0 OR v_cross_merchant > 0 OR v_missing_service > 0 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Product price sync foreign-key preflight failed; resolve the rows returned above first';
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.statistics
        WHERE table_schema = DATABASE()
          AND table_name = 'product'
          AND index_name = 'uk_product_id_merchant_id'
    ) THEN
        ALTER TABLE product
            ADD UNIQUE KEY uk_product_id_merchant_id (id, merchant_id);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.statistics
        WHERE table_schema = DATABASE()
          AND table_name = 'mall_booking_service'
          AND index_name = 'idx_booking_service_product_merchant'
    ) THEN
        ALTER TABLE mall_booking_service
            ADD KEY idx_booking_service_product_merchant (product_id, merchant_id);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.referential_constraints
        WHERE constraint_schema = DATABASE()
          AND table_name = 'mall_booking_service'
          AND constraint_name = 'fk_booking_service_product_merchant'
    ) THEN
        ALTER TABLE mall_booking_service
            ADD CONSTRAINT fk_booking_service_product_merchant
            FOREIGN KEY (product_id, merchant_id)
            REFERENCES product (id, merchant_id)
            ON DELETE RESTRICT ON UPDATE RESTRICT;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.referential_constraints
        WHERE constraint_schema = DATABASE()
          AND table_name = 'mall_booking'
          AND constraint_name = 'fk_booking_record_service'
    ) THEN
        ALTER TABLE mall_booking
            ADD CONSTRAINT fk_booking_record_service
            FOREIGN KEY (booking_service_id)
            REFERENCES mall_booking_service (id)
            ON DELETE RESTRICT ON UPDATE RESTRICT;
    END IF;
END $$

CALL enforce_product_price_sync_constraints() $$
DROP PROCEDURE IF EXISTS enforce_product_price_sync_constraints $$

DELIMITER ;
