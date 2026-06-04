USE `ruoyi-cs`;

-- 目的：
-- 1. 修复分销商逻辑删除后，旧 username 仍占用 uk_username 唯一索引的问题
-- 2. 让已删除分销商的登录账号可被新分销商复用

-- 执行前可先查看哪些历史删除数据仍占着原账号
SELECT id, name, username, del_flag, update_time
FROM distributor
WHERE del_flag = '2'
  AND username IS NOT NULL
  AND username <> CONCAT('deleted_', id)
ORDER BY id;

-- 把历史已删除分销商的 username 统一改成 deleted_{id}
UPDATE distributor
SET username = CONCAT('deleted_', id),
    update_time = NOW()
WHERE del_flag = '2'
  AND username IS NOT NULL
  AND username <> CONCAT('deleted_', id);

-- 执行后复核
SELECT id, name, username, del_flag, update_time
FROM distributor
WHERE del_flag = '2'
ORDER BY id;
