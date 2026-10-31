CREATE DATABASE chippy_library CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE chippy_library;
CREATE TABLE `user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username` VARCHAR(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '登录账号',
    `password` VARCHAR(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码',
    `nick_name` VARCHAR(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '昵称',
    `avatar` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '头像路径',
    `phone` VARCHAR(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '手机号',
    `sex` VARCHAR(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '性别',
    `address` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '地址',
    `role` INT NOT NULL DEFAULT 2 COMMENT '角色:1=管理员， 2=普通用户',
    `create_time` DATETIME NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
    `update_time` DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `username` (`username`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户表' ROW_FORMAT = Dynamic;

CREATE TABLE `book`(
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '书籍ID',
    `owner_id` bigint NOT NULL COMMENT '所有者ID(关联user.id)',
    `isbn` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'ISBN编号',
    `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '书名',
    `author` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '作者',
    `publisher` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '出版社',
    `price` decimal(10, 2) NULL DEFAULT NULL COMMENT '价格',
    `cover` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '封面图片路径',
    `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '书籍简介',
    `read_status` int NOT NULL DEFAULT 0 COMMENT '阅读状态:0=想读,1=在读,2=已读',
    `progress` int NOT NULL DEFAULT 0 COMMENT '阅读进度(0-100)',
    `rating` int NULL DEFAULT NULL COMMENT '个人评分(1-5星)',
    `total_pages` int NULL DEFAULT NULL COMMENT '总页数',
    `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '添加时间',
    `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `owner_id` (`owner_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '书籍表' ROW_FORMAT = Dynamic;

CREATE TABLE `book_tag`(
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '标签ID',
    `book_id` bigint NOT NULL COMMENT '书籍ID',
    `tag_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '标签名',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `book_id`(`book_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '书籍标签表' ROW_FORMAT = Dynamic;

CREATE TABLE `note` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '笔记ID',
  `book_id` bigint NOT NULL COMMENT '书籍ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '笔记内容',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `book_id`(`book_id`) USING BTREE,
  INDEX `user_id`(`user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '读书笔记表' ROW_FORMAT = Dynamic;

CREATE TABLE `lend_record`(
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    `book_id` bigint NOT NULL COMMENT '书籍ID',
    `lender_id` bigint NOT NULL COMMENT '借出方（书的主人）',
    `borrower_id` bigint NOT NULL COMMENT '借入方（借书的人）',
    `lend_time` datetime NOT NULL COMMENT '借出时间',
    `return_time` datetime NULL DEFAULT NULL COMMENT '归还时间(null表示未还)',
    `status` int NOT NULL DEFAULT 0 COMMENT '状态:0=借出中,1=已归还',
    `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `book_id`(`book_id`) USING BTREE,
    INDEX `lender_id`(`lender_id`) USING BTREE,
    INDEX `borrower_id`(`borrower_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '借阅记录表' ROW_FORMAT = Dynamic;


INSERT INTO `user` (`username`, `password`, `nick_name`, `role`) 
VALUES ('admin', '123456', '管理员', 1);

INSERT INTO `user` (`username`, `password`, `nick_name`, `role`) 
VALUES ('user1', '123456', '小明', 2);

SELECT * FROM user;