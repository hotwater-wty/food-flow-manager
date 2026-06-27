-- food-flow-manager V1 schema
-- MySQL 8.x
-- Charset: utf8mb4

SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

CREATE DATABASE IF NOT EXISTS `food_flow_manager`
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_0900_ai_ci;

USE `food_flow_manager`;

-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `phone` VARCHAR(20) NOT NULL COMMENT '手机号',
  `password` VARCHAR(255) NOT NULL COMMENT '加密后的密码',
  `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '用户状态：1正常，2禁用',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_phone` (`phone`),
  KEY `idx_user_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户表';

-- 员工表
CREATE TABLE IF NOT EXISTS `employee` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `phone` VARCHAR(20) NOT NULL COMMENT '手机号',
  `password` VARCHAR(255) NOT NULL COMMENT '加密后的密码',
  `name` VARCHAR(50) NOT NULL COMMENT '员工姓名',
  `role` TINYINT NOT NULL COMMENT '员工角色：1店员，2店长',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '员工状态：1正常，2禁用，3离职',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_employee_phone` (`phone`),
  KEY `idx_employee_role` (`role`),
  KEY `idx_employee_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='员工表';

-- 桌位表
CREATE TABLE IF NOT EXISTS `dining_table` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `table_no` VARCHAR(20) NOT NULL COMMENT '桌号',
  `capacity` INT UNSIGNED NOT NULL COMMENT '建议容纳人数',
  `location_desc` VARCHAR(100) DEFAULT NULL COMMENT '位置描述',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '桌位状态：0空闲，1已预约，2等待中，3用餐中，4禁用',
  `current_session_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '当前开台会话ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_dining_table_table_no` (`table_no`),
  KEY `idx_dining_table_status` (`status`),
  KEY `idx_dining_table_current_session_id` (`current_session_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='桌位表';

-- 预约表
CREATE TABLE IF NOT EXISTS `reservation` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `reservation_no` VARCHAR(32) NOT NULL COMMENT '预约编号',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '预约用户ID',
  `table_id` BIGINT UNSIGNED NOT NULL COMMENT '预约桌位ID',
  `people_count` INT UNSIGNED NOT NULL COMMENT '预约人数',
  `reserve_time` DATETIME NOT NULL COMMENT '预约到店时间',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '预约状态：0待到店，1已到店，2已取消，3已超时',
  `check_in_time` DATETIME DEFAULT NULL COMMENT '实际到店时间',
  `cancel_time` DATETIME DEFAULT NULL COMMENT '取消时间',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_reservation_no` (`reservation_no`),
  KEY `idx_reservation_user_id` (`user_id`),
  KEY `idx_reservation_table_id` (`table_id`),
  KEY `idx_reservation_status` (`status`),
  KEY `idx_reservation_reserve_time` (`reserve_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='预约表';

-- 开台会话表
CREATE TABLE IF NOT EXISTS `dining_session` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `session_no` VARCHAR(32) NOT NULL COMMENT '开台编号',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  `table_id` BIGINT UNSIGNED NOT NULL COMMENT '桌位ID',
  `reservation_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '预约ID，未预约扫码占座时为空',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '开台状态：0等待中，1用餐中，2已完成，3已取消',
  `open_time` DATETIME NOT NULL COMMENT '扫码开台时间',
  `first_order_time` DATETIME DEFAULT NULL COMMENT '首次下单时间',
  `close_time` DATETIME DEFAULT NULL COMMENT '结束时间',
  `close_employee_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '清台或释放桌位的员工ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_dining_session_no` (`session_no`),
  KEY `idx_dining_session_user_id` (`user_id`),
  KEY `idx_dining_session_table_id` (`table_id`),
  KEY `idx_dining_session_reservation_id` (`reservation_id`),
  KEY `idx_dining_session_status` (`status`),
  KEY `idx_dining_session_open_time` (`open_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='开台会话表';

-- 菜品分类表
CREATE TABLE IF NOT EXISTS `dish_category` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` VARCHAR(50) NOT NULL COMMENT '分类名称',
  `sort` INT NOT NULL DEFAULT 0 COMMENT '排序值',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1启用，2禁用',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_dish_category_name` (`name`),
  KEY `idx_dish_category_status` (`status`),
  KEY `idx_dish_category_sort` (`sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='菜品分类表';

-- 菜品表
CREATE TABLE IF NOT EXISTS `dish` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `category_id` BIGINT UNSIGNED NOT NULL COMMENT '菜品分类ID',
  `name` VARCHAR(100) NOT NULL COMMENT '菜品名称',
  `price` INT UNSIGNED NOT NULL COMMENT '价格，单位分',
  `image` VARCHAR(255) DEFAULT NULL COMMENT '配图地址',
  `description` VARCHAR(500) DEFAULT NULL COMMENT '描述信息',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '菜品状态：0停售，1启售，2售罄',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_dish_category_id` (`category_id`),
  KEY `idx_dish_status` (`status`),
  KEY `idx_dish_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='菜品表';

-- 堂食订单表
CREATE TABLE IF NOT EXISTS `dining_order` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `order_no` VARCHAR(32) NOT NULL COMMENT '订单编号',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  `table_id` BIGINT UNSIGNED NOT NULL COMMENT '桌位ID',
  `session_id` BIGINT UNSIGNED NOT NULL COMMENT '开台会话ID',
  `total_amount` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '订单总金额，单位分',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '订单状态：1已下单，2制作中，3已上齐，4已完成，5已取消',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_dining_order_no` (`order_no`),
  KEY `idx_dining_order_user_id` (`user_id`),
  KEY `idx_dining_order_table_id` (`table_id`),
  KEY `idx_dining_order_session_id` (`session_id`),
  KEY `idx_dining_order_status` (`status`),
  KEY `idx_dining_order_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='堂食订单表';

-- 订单明细表
CREATE TABLE IF NOT EXISTS `order_item` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `order_id` BIGINT UNSIGNED NOT NULL COMMENT '订单ID',
  `dish_id` BIGINT UNSIGNED NOT NULL COMMENT '菜品ID',
  `dish_name` VARCHAR(100) NOT NULL COMMENT '下单时菜品名称',
  `dish_image` VARCHAR(255) DEFAULT NULL COMMENT '下单时菜品图片',
  `dish_price` INT UNSIGNED NOT NULL COMMENT '下单时菜品单价，单位分',
  `quantity` INT UNSIGNED NOT NULL COMMENT '数量',
  `amount` INT UNSIGNED NOT NULL COMMENT '该明细总金额，单位分',
  `remark` VARCHAR(200) DEFAULT NULL COMMENT '口味或备注',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_order_item_order_id` (`order_id`),
  KEY `idx_order_item_dish_id` (`dish_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='订单明细表';

-- V1 初始化数据
INSERT INTO `employee` (`phone`, `password`, `name`, `role`, `status`)
SELECT '18800000000', '$2a$10$KMER9E1bKi1TUMhLnJ7RGO7lrm5avQibzF4ywJExlpVhsT6Oy6l02', '默认店长', 2, 1
WHERE NOT EXISTS (
  SELECT 1 FROM `employee` WHERE `phone` = '18800000000'
);

INSERT INTO `dish_category` (`name`, `sort`, `status`)
SELECT '热菜', 10, 1
WHERE NOT EXISTS (
  SELECT 1 FROM `dish_category` WHERE `name` = '热菜'
);

INSERT INTO `dish_category` (`name`, `sort`, `status`)
SELECT '主食', 20, 1
WHERE NOT EXISTS (
  SELECT 1 FROM `dish_category` WHERE `name` = '主食'
);

INSERT INTO `dish_category` (`name`, `sort`, `status`)
SELECT '饮品', 30, 1
WHERE NOT EXISTS (
  SELECT 1 FROM `dish_category` WHERE `name` = '饮品'
);
