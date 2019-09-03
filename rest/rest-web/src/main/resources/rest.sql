CREATE DATABASE im;
DROP TABLE IF EXISTS `im_user`;
CREATE TABLE `im_user` (
                         `id` bigint(20) NOT NULL,
                         `username` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '用户名',
                         `pwd_hash` char(128) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '密码加密后的hash值',
                         `salt` char(16) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '盐',
                         `gmt_create` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         `gmt_update` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                         `deleted` tinyint(1) NOT NULL DEFAULT '0',
                         PRIMARY KEY (`id`),
                         UNIQUE KEY `username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `im_relation`;
CREATE TABLE `im_relation` (
                             `id` bigint(20) NOT NULL,
                             `user_id1` varchar(100) NOT NULL COMMENT '用户1的id',
                             `user_id2` varchar(100) NOT NULL COMMENT '用户2的id',
                             `encrypt_key` char(33) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '密钥',
                             `gmt_create` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
                             `gmt_update` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                             `deleted` tinyint(1) NOT NULL DEFAULT '0',
                             PRIMARY KEY (`id`),
                             UNIQUE KEY `USERID1_USERID2` (`user_id1`,`user_id2`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `im_offline`;
CREATE TABLE `im_offline` (
                            `id` bigint(20) NOT NULL,
                            `msg_id` bigint(20) NOT NULL,
                            `msg_code` int(2) NOT NULL,
                            `to_user_id` varchar(100) NOT NULL,
                            `content` varbinary(5000) NOT NULL DEFAULT '',
                            `has_read` tinyint(1) NOT NULL DEFAULT '0',
                            `gmt_create` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            `gmt_update` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                            `deleted` tinyint(1) NOT NULL DEFAULT '0'
) ENGINE=InnoDB DEFAULT CHARSET=utf8;