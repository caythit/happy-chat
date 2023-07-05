CREATE DATABASE `happychat`;

CREATE TABLE `user_info` (
    `id` BIGINT(11) UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '主键id',
    `create_time` BIGINT(11) UNSIGNED NOT NULL COMMENT '记录创建时间',
    `update_time` BIGINT(11) UNSIGNED NOT NULL COMMENT '记录更新时间',
    `user_id` VARCHAR(100) NOT NULL COMMENT 'uuid',
    `user_name` VARCHAR(200) NOT NULL COMMENT '用户名',
    `user_pwd` VARCHAR(200) NOT NULL COMMENT '用户密码（加密）',
    UNIQUE INDEX `uniq_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4

userId,robotId,messageId
CREATE TABLE `user_robot_bind` (
    `id` BIGINT(11) UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '主键id',
    `create_time` BIGINT(11) UNSIGNED NOT NULL COMMENT '记录创建时间',
    `update_time` BIGINT(11) UNSIGNED NOT NULL COMMENT '记录更新时间',
    `robot_id` VARCHAR(100) NOT NULL COMMENT 'uuid',
    `robot_sex` tinyint(1) unsigned NOT NULL COMMENT '0：man，1：women',
    `robot_age` int(3) unsigned NOT NULL COMMENT 'age',
    `robot_country` VARCHAR(5) NOT NULL NOT NULL COMMENT '國家'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4

CREATE TABLE `robot_info` (
    `id` BIGINT(11) UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '主键id',
    `create_time` BIGINT(11) UNSIGNED NOT NULL COMMENT '记录创建时间',
    `update_time` BIGINT(11) UNSIGNED NOT NULL COMMENT '记录更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4

CREATE TABLE `chat_message` (
    `id` BIGINT(11) UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '主键id',
    `create_time` BIGINT(11) UNSIGNED NOT NULL COMMENT '记录创建时间',
    `update_time` BIGINT(11) UNSIGNED NOT NULL COMMENT '记录更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4

CREATE TABLE `user_payment_request` (
    `id` BIGINT(11) UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '主键id',
    `create_time` BIGINT(11) UNSIGNED NOT NULL COMMENT '记录创建时间',
    `update_time` BIGINT(11) UNSIGNED NOT NULL COMMENT '记录更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4

CREATE TABLE `user_payment_result` (
    `id` BIGINT(11) UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '主键id',
    `create_time` BIGINT(11) UNSIGNED NOT NULL COMMENT '记录创建时间',
    `update_time` BIGINT(11) UNSIGNED NOT NULL COMMENT '记录更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4

