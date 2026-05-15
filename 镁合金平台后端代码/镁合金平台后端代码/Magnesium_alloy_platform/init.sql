-- 创建数据库
CREATE DATABASE IF NOT EXISTS `model_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `model_db`;

-- 1. 用户表
CREATE TABLE IF NOT EXISTS `user` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `username` VARCHAR(255) NOT NULL,
    `password` VARCHAR(255) NOT NULL,
    `uid` VARCHAR(64) UNIQUE NOT NULL,
    `email` VARCHAR(255),
    `UserUrl` VARCHAR(255)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. BP参数表
CREATE TABLE IF NOT EXISTS `bp_parameter` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `input_layer` INT,
    `output_layer` INT,
    `intermediate_layer` INT,
    `options` VARCHAR(255),
    `number_of_cycles` INT,
    `learning_rate` DOUBLE,
    `error_target_value` DOUBLE,
    `create_user` VARCHAR(64),
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. PSO参数表
CREATE TABLE IF NOT EXISTS `pso_parameter` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `max_num` INT,
    `top_value` INT,
    `low_value` INT,
    `swarm_size` INT,
    `individual_factor` INT,
    `group_factor` INT,
    `inertia_factor` DOUBLE,
    `create_user` VARCHAR(64),
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4. GA参数表
CREATE TABLE IF NOT EXISTS `ga_parameter` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `crossover_probability` DOUBLE,
    `variation_probability` DOUBLE,
    `create_user` VARCHAR(64),
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5. 通用Excel历史记录表
CREATE TABLE IF NOT EXISTS `excel_history` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `ave_length` DOUBLE,
    `lisan_value` DOUBLE,
    `hardness` DOUBLE,
    `yield_strength` DOUBLE,
    `strength_extension` DOUBLE,
    `uid` VARCHAR(64),
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 6. BP历史记录表
CREATE TABLE IF NOT EXISTS `bp_history` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `ave_length` DOUBLE,
    `lisan_value` DOUBLE,
    `hardness` DOUBLE,
    `yield_strength` DOUBLE,
    `strength_extension` DOUBLE,
    `input_layer` INT,
    `output_layer` INT,
    `intermediate_layer` INT,
    `options` VARCHAR(255),
    `number_of_cycles` INT,
    `learning_rate` DOUBLE,
    `error_target_value` DOUBLE,
    `uid` VARCHAR(64),
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 7. PSO历史记录表
CREATE TABLE IF NOT EXISTS `pso_history` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `ave_length` DOUBLE,
    `lisan_value` DOUBLE,
    `hardness` DOUBLE,
    `yield_strength` DOUBLE,
    `strength_extension` DOUBLE,
    `max_num` INT,
    `top_value` DOUBLE,
    `low_value` DOUBLE,
    `swarm_size` INT,
    `individual_factor` INT,
    `group_factor` INT,
    `inertia_factor` DOUBLE,
    `uid` VARCHAR(64),
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 8. 图像信息表
CREATE TABLE IF NOT EXISTS `image` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `ImageUrl` VARCHAR(500) NOT NULL,
    `BinaryUrl` VARCHAR(500),
    `createTime` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `uid` VARCHAR(64)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 9. 缺陷分析表
CREATE TABLE IF NOT EXISTS `crack` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `CrackStart` VARCHAR(255),
    `CrackLength` DOUBLE,
    `CrackWidth` DOUBLE,
    `CrackHeight` DOUBLE,
    `CrackArea` DOUBLE,
    `CrackPerimeter` DOUBLE,
    `ImageUrl` VARCHAR(500)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 10. 操作日志表
CREATE TABLE IF NOT EXISTS `operate_log` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `operate_user` INT,
    `operate_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `class_name` VARCHAR(255),
    `method_name` VARCHAR(255),
    `method_params` TEXT,
    `return_value` TEXT,
    `cost_time` BIGINT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
