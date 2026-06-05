CREATE TABLE IF NOT EXISTS `user_account_cancel_record` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `app_id` varchar(64) NOT NULL COMMENT 'C端小程序AppID',
  `open_id_hash` varchar(128) NOT NULL COMMENT '微信OpenID的SHA-256哈希',
  `user_id` varchar(64) NOT NULL COMMENT '注销前平台用户id',
  `cancel_time` datetime NOT NULL COMMENT '注销时间',
  `allow_register_time` datetime NOT NULL COMMENT '允许重新注册时间',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_app_open_hash` (`app_id`, `open_id_hash`),
  KEY `idx_allow_register_time` (`allow_register_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='小程序用户注销重新注册限制记录';
