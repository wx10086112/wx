-- 2026-06-29 payment callback/refund hotfix.
-- Keep WeChat callback writes from breaking payment state updates.

ALTER TABLE `payment_record`
  MODIFY COLUMN `notify_result` TEXT DEFAULT NULL COMMENT 'payment/refund callback result summary or raw payload';

