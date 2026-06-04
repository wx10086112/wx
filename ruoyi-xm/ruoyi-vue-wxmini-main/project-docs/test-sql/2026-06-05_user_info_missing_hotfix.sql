CREATE TABLE IF NOT EXISTS `user_info` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `user_id` varchar(64) NOT NULL COMMENT '平台用户id',
  `user_name` varchar(64) NOT NULL DEFAULT '微信用户' COMMENT '用户名',
  `user_type` varchar(64) NOT NULL DEFAULT '1' COMMENT '用户类型',
  `phone` varchar(64) DEFAULT NULL COMMENT '手机号',
  `open_id` varchar(128) DEFAULT NULL COMMENT '微信用户唯一标识',
  `union_id` varchar(128) DEFAULT NULL COMMENT '微信全平台用户唯一标识',
  `avatar_url` varchar(256) DEFAULT NULL COMMENT '用户头像',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `del_flag` char(1) NOT NULL DEFAULT '0' COMMENT '删除标志(0存在 2删除)',
  PRIMARY KEY (`id`),
  KEY `user_info_index_user_id` (`user_id`) USING BTREE,
  KEY `user_info_index_open_id` (`open_id`) USING BTREE,
  KEY `user_info_index_phone` (`phone`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户信息表';
