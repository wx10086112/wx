USE `ruoyi-cs`;

DROP PROCEDURE IF EXISTS add_merchant_carousel_images_if_missing;

DELIMITER //

CREATE PROCEDURE add_merchant_carousel_images_if_missing()
BEGIN
  IF NOT EXISTS (
    SELECT 1
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'merchant'
      AND column_name = 'carousel_images'
  ) THEN
    ALTER TABLE `merchant`
      ADD COLUMN `carousel_images` VARCHAR(2000) DEFAULT ''
      COMMENT '店铺轮播图路径，逗号分隔'
      AFTER `avatar`;
  END IF;
END//

DELIMITER ;

CALL add_merchant_carousel_images_if_missing();

DROP PROCEDURE IF EXISTS add_merchant_carousel_images_if_missing;
