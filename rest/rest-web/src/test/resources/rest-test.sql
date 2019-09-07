CREATE TABLE `im_user`
(
  `id`         bigint(20)  NOT NULL,
  `username`   varchar(50) NOT NULL DEFAULT '' COMMENT '用户名',
  `pwd_hash`   char(128)   NOT NULL DEFAULT '' COMMENT '密码加密后的hash值',
  `salt`       char(16)    NOT NULL DEFAULT '' COMMENT '盐',
  `gmt_create` timestamp   NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `gmt_update` timestamp   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`    tinyint(1)  NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`)
) DEFAULT CHARSET = utf8;
INSERT INTO `im_user` (`id`, `username`, `pwd_hash`, `salt`, `gmt_create`, `gmt_update`, `deleted`)
VALUES (1119861162352148481, 'xianyy', '16c91598eb3e913560b56f05a1baf5bd60a9e21f397d691d93808a8551df32be',
        'vGKIys&hO!m9L;Gb', '2019-04-21 15:11:21', '2019-05-14 14:27:55', 0),
       (1142773797275836418, 'yuanrw', '93a338717d730b1895978b242022aa6bca980fcf0e9bd527c9dd6ce375baf3bd',
        'KSILK>LF2d^<UDDK', '2019-06-23 20:37:59', '2019-06-23 20:37:59', 0),
       (1142788235445940225, 'Alice', '9de1765ffb0ab9e5dffa7cad1d4562c27a190fdb758615e420504afea9ec736f',
        'eESWb t/]+Zh0Tns', '2019-06-23 21:35:21', '2019-06-23 21:35:21', 0),
       (1142784917944406018, 'Tom', '4f46ef896b7942e00afa6abb649daa686709154fce69217e52095e1bd7fb0ebe',
        'T$3N_sI_x._a|I^2', '2019-06-23 21:22:11', '2019-06-23 21:22:33', 0);

CREATE TABLE `im_relation`
(
  `id`          bigint(20)   NOT NULL,
  `user_id1`    varchar(100) NOT NULL COMMENT '用户1的id',
  `user_id2`    varchar(100) NOT NULL COMMENT '用户2的id',
  `encrypt_key` char(33)     NOT NULL DEFAULT '' COMMENT '密钥',
  `gmt_create`  timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `gmt_update`  timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`     tinyint(1)   NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
) DEFAULT CHARSET = utf8;
INSERT INTO `im_relation` (`id`, `user_id1`, `user_id2`, `encrypt_key`, `gmt_create`, `gmt_update`, `deleted`)
VALUES (1126372502323335170, '1119861162352148481', '1142773797275836418', 'HvxZFa7B1dBlKwP7|9302073163544974',
        '2019-05-09 14:25:06', '2019-06-23 20:39:50', 0),
       (1142785399664414722, '1142788235445940225', '1142784917944406018', 'PucSAdsmh48SqDEH|9290294155740712',
        '2019-06-23 21:24:05', '2019-06-23 21:24:05', 0),
       (1142785428735135746, '1142773797275836418', '1142784917944406018', 'oWbuNUhCw2alTxZ0|7701036262872990',
        '2019-06-23 21:24:12', '2019-06-23 21:24:12', 0);

CREATE TABLE `im_offline`
(
  `id`         bigint(20)      NOT NULL,
  `msg_id`     bigint(20)      NOT NULL,
  `msg_code`   int(2)          NOT NULL,
  `to_user_id` varchar(100)    NOT NULL,
  `content`    varbinary(5000) NOT NULL DEFAULT '',
  `has_read`   tinyint(1)      NOT NULL DEFAULT '0',
  `gmt_create` timestamp       NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `gmt_update` timestamp       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`    tinyint(1)      NOT NULL DEFAULT '0'
) DEFAULT CHARSET = utf8;
INSERT INTO `im_offline` (`id`, `msg_id`, `msg_code`, `to_user_id`, `content`, `has_read`, `gmt_create`, `gmt_update`,
                          `deleted`)
VALUES (1147083599569240065, 344581161395294208, 0, '1142773797275836418',
        X'08011080A088CAD9D78CE40418002082C080AAECD6FCED0F28818086DEB3F9A2C50F30C5ADDB8CBC2D38004218734C4B5676466F37494D794232666A765A72417365513D3D',
        0, '2019-07-05 18:03:36', '2019-07-05 18:03:36', 0),
       (1147083668280328194, 344581230265765888, 0, '1142773797275836418',
        X'08011080A08892DAD98CE40418002082C080AAECD6FCED0F28818086DEB3F9A2C50F30E9ADDC8CBC2D38004218734C4B5676466F37494D794232666A765A72417365513D3D',
        0, '2019-07-05 18:03:52', '2019-07-05 18:03:52', 0);