-- Enable System Management > User Management for the built-in super administrator only.
-- The built-in super administrator (user_id = 1) receives all enabled menus directly,
-- so no sys_role_menu association is required.

UPDATE sys_menu
SET visible = '0',
    status = '0'
WHERE menu_id IN (100, 1000, 1001, 1002, 1003, 1004, 1005, 1006);

DELETE FROM sys_role_menu
WHERE menu_id IN (100, 1000, 1001, 1002, 1003, 1004, 1005, 1006);
