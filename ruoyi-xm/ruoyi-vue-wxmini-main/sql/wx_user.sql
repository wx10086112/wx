drop table if exists user_info;
CREATE TABLE `user_info` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `user_id` varchar(64) NOT NULL COMMENT '平台用户id',
  `user_name` varchar(64) NOT NULL DEFAULT '微信用户' COMMENT '用户名',
  `user_type` varchar(64) NOT NULL DEFAULT '1' COMMENT '用户类型',
  `phone` varchar(64) NULL COMMENT '手机号',
  `open_id` varchar(128) NULL COMMENT '微信用户唯一标识',
  `union_id` varchar(128) NULL COMMENT '微信全平台用户唯一标识',
  `avatar_url` varchar(256) NULL DEFAULT 'https://mmbiz.qpic.cn/mmbiz/icTdbqWNOwNRna42FI242Lcia07jQodd2FJGIYQfG0LAJGFxM4FbnQP6yfMxBgJ0F3YRqJCJ1aPAK2dQagdusBZg/0' COMMENT '用户头像',
  `create_time` datetime NULL COMMENT '创建时间',
  `update_time` datetime NULL COMMENT '更新时间',
  `del_flag` char(1) NOT NULL DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
  PRIMARY KEY (`id`),
  INDEX `user_info_index_user_id`(`user_id`) USING BTREE,
  INDEX `user_info_index_open_id`(`open_id`) USING BTREE,
  INDEX `user_info_index_phone`(`phone`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 comment = '用户信息表';

drop table if exists user_account_cancel_record;
CREATE TABLE `user_account_cancel_record` (
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
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='小程序用户注销重新注册限制记录';
