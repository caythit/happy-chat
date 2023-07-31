CREATE DATABASE `Flirtopia`;

CREATE TABLE `user_info` (
    `id` BIGINT(11) UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '主键id',
    `create_time` BIGINT(11) UNSIGNED NOT NULL COMMENT '记录创建时间',
    `update_time` BIGINT(11) UNSIGNED NOT NULL COMMENT '记录更新时间',
    `dummy_user_id` VARCHAR(50) NOT NULL COMMENT 'uuid',
    `user_prefer_info` VARCHAR(128) DEFAULT NULL COMMENT 'prefrer信息',
    `user_id` VARCHAR(50) DEFAULT NULL COMMENT '等于dummy_user_id，注册后才有',
    `user_name` VARCHAR(50) DEFAULT NULL COMMENT '用户名',
    `user_pwd` VARCHAR(128) DEFAULT NULL COMMENT '用户密码（加密）',
    `email` VARCHAR(50) DEFAULT NULL COMMENT 'mail',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机',
    `pwd_salt` varchar(50) DEFAULT NULL COMMENT '密码加盐',
    `extra_info` text COLLATE utf8mb4_bin COMMENT '额外信息',
    UNIQUE INDEX `uniq_dummy_user_id` (`dummy_user_id`),
    UNIQUE INDEX `uniq_user_id` (`user_id`),
    UNIQUE INDEX `uniq_email` (`email`),
    KEY `name` (`user_name`),
    KEY `phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `robot_info` (
    `id` BIGINT(11) UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '主键id',
    `create_time` BIGINT(11) UNSIGNED NOT NULL COMMENT '记录创建时间',
    `update_time` BIGINT(11) UNSIGNED NOT NULL COMMENT '记录更新时间',
    `robot_id` VARCHAR(50) NOT NULL COMMENT 'uuid',
    `head_url` VARCHAR(128) NOT NULL COMMENT '头像',
    `cover_url` VARCHAR(128) NOT NULL COMMENT '封面',
    `bg_url` VARCHAR(128) NOT NULL COMMENT '背景图，目前按一张来',
    `name` VARCHAR(50) NOT NULL COMMENT 'name',
    `sex` tinyint(1) UNSIGNED NOT NULL COMMENT '1:man,2:women',
    `age` tinyint(5) UNSIGNED NOT NULL COMMENT 'age',
    `city` VARCHAR(50) NOT NULL COMMENT '城市',
    `country` VARCHAR(50) NOT NULL COMMENT 'country',
    `extra_info` text COLLATE utf8mb4_bin COMMENT '额外信息，比如职业、兴趣、介绍语',
    UNIQUE INDEX `uniq_robot_id` (`robot_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `robot_ice_break_word` (
    `id` BIGINT(11) UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '主键id',
    `create_time` BIGINT(11) UNSIGNED NOT NULL COMMENT '记录创建时间',
    `update_time` BIGINT(11) UNSIGNED NOT NULL COMMENT '记录更新时间',
    `robot_id` VARCHAR(50) NOT NULL COMMENT 'uuid',
    `content` text COLLATE utf8mb4_bin COMMENT '内容',
    `source` VARCHAR(50) DEFAULT NULL COMMENT '来源，默认人工',
    `extra_info` text COLLATE utf8mb4_bin COMMENT '额外信息'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `chat_message` (
    `id` BIGINT(11) UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '主键id',
    `create_time` BIGINT(11) UNSIGNED NOT NULL COMMENT '记录创建时间',
    `update_time` BIGINT(11) UNSIGNED NOT NULL COMMENT '记录更新时间',
    `user_id` VARCHAR(100) NOT NULL COMMENT 'uuid',
    `robot_id` VARCHAR(100) NOT NULL COMMENT 'uuid',
    `message_id` VARCHAR(50) NOT NULL COMMENT 'uuid',
    `message_type` VARCHAR(10) NOT NULL COMMENT '消息类型',
    `message_from` VARCHAR(10) NOT NULL COMMENT '消息发自谁，robot or user',
    `content` text COLLATE utf8mb4_bin COMMENT '消息内容',
    `extra_info` text COLLATE utf8mb4_bin COMMENT '额外信息',
     KEY `user_id` (`user_id`),
     KEY `robot_id` (`robot_id`),
     KEY `message_id` (`message_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 支付--
CREATE TABLE `user_payment_request` (
    `id` BIGINT(11) UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '主键id',
    `create_time` BIGINT(11) UNSIGNED NOT NULL COMMENT '记录创建时间',
    `update_time` BIGINT(11) UNSIGNED NOT NULL COMMENT '记录更新时间',
    `requestId` VARCHAR(100) NOT NULL COMMENT 'uuid',
    PRIMARY KEY (`id`),
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4

CREATE TABLE `user_payment_result` (
    `id` BIGINT(11) UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '主键id',
    `create_time` BIGINT(11) UNSIGNED NOT NULL COMMENT '记录创建时间',
    `update_time` BIGINT(11) UNSIGNED NOT NULL COMMENT '记录更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4

