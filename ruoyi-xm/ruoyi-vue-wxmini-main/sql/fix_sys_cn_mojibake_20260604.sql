-- 修复系统基础中文数据在 Windows cp850 导入链路中产生的乱码
-- 适用场景：中文 UTF-8 被错误按 cp850 解释后再次写回 MySQL

START TRANSACTION;

UPDATE sys_role
SET role_name = CONVERT(CAST(CONVERT(role_name USING cp850) AS BINARY) USING utf8mb4)
WHERE HEX(role_name) REGEXP '^(C2|C3|E2)';

UPDATE sys_menu
SET menu_name = CONVERT(CAST(CONVERT(menu_name USING cp850) AS BINARY) USING utf8mb4)
WHERE HEX(menu_name) REGEXP '^(C2|C3|E2)';

UPDATE sys_dept
SET dept_name = CONVERT(CAST(CONVERT(dept_name USING cp850) AS BINARY) USING utf8mb4)
WHERE HEX(dept_name) REGEXP '^(C2|C3|E2)';

UPDATE sys_post
SET post_name = CONVERT(CAST(CONVERT(post_name USING cp850) AS BINARY) USING utf8mb4)
WHERE HEX(post_name) REGEXP '^(C2|C3|E2)';

UPDATE sys_dict_type
SET dict_name = CONVERT(CAST(CONVERT(dict_name USING cp850) AS BINARY) USING utf8mb4)
WHERE HEX(dict_name) REGEXP '^(C2|C3|E2)';

UPDATE sys_config
SET config_name = CONVERT(CAST(CONVERT(config_name USING cp850) AS BINARY) USING utf8mb4)
WHERE HEX(config_name) REGEXP '^(C2|C3|E2)';

UPDATE sys_config
SET remark = CONVERT(CAST(CONVERT(remark USING cp850) AS BINARY) USING utf8mb4)
WHERE remark IS NOT NULL
  AND remark <> ''
  AND HEX(remark) REGEXP '^(C2|C3|E2)';

UPDATE sys_job
SET job_name = CONVERT(CAST(CONVERT(job_name USING cp850) AS BINARY) USING utf8mb4)
WHERE HEX(job_name) REGEXP '^(C2|C3|E2)';

UPDATE sys_job_log
SET job_name = CONVERT(CAST(CONVERT(job_name USING cp850) AS BINARY) USING utf8mb4)
WHERE HEX(job_name) REGEXP '^(C2|C3|E2)';

UPDATE sys_notice
SET notice_title = CONVERT(CAST(CONVERT(notice_title USING cp850) AS BINARY) USING utf8mb4)
WHERE HEX(notice_title) REGEXP '^(C2|C3|E2)';

UPDATE sys_oper_log
SET dept_name = CONVERT(CAST(CONVERT(dept_name USING cp850) AS BINARY) USING utf8mb4)
WHERE dept_name IS NOT NULL
  AND dept_name <> ''
  AND HEX(dept_name) REGEXP '^(C2|C3|E2)';

COMMIT;
