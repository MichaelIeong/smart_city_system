/*
 Navicat Premium Dump SQL

 Source Server         : 5090
 Source Server Type    : MySQL
 Source Server Version : 80044 (8.0.44)
 Source Host           : 10.176.65.202:3306
 Source Schema         : smart_city

 Target Server Type    : MySQL
 Target Server Version : 80044 (8.0.44)
 File Encoding         : 65001

 Date: 23/04/2026 11:22:51
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for actuating_function_device
-- ----------------------------
DROP TABLE IF EXISTS `actuating_function_device`;
CREATE TABLE `actuating_function_device`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `actuating_function_id` int NOT NULL,
  `device_id` int NOT NULL,
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `FKabnu1foahdw7x84s828rv7ugc`(`actuating_function_id` ASC) USING BTREE,
  INDEX `FK1njd15amieyct3hjmglv7con7`(`device_id` ASC) USING BTREE,
  CONSTRAINT `FK1njd15amieyct3hjmglv7con7` FOREIGN KEY (`device_id`) REFERENCES `devices` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `FKabnu1foahdw7x84s828rv7ugc` FOREIGN KEY (`actuating_function_id`) REFERENCES `actuating_functions` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 16 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for actuating_function_device_type
-- ----------------------------
DROP TABLE IF EXISTS `actuating_function_device_type`;
CREATE TABLE `actuating_function_device_type`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `actuating_function_id` int NOT NULL,
  `device_type_id` int NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `FK6l2fhb3bt8wpvl5ppp5rvi26t`(`actuating_function_id` ASC) USING BTREE,
  INDEX `FK2c25pm5d5s86dt999m3ib2umv`(`device_type_id` ASC) USING BTREE,
  CONSTRAINT `FK2c25pm5d5s86dt999m3ib2umv` FOREIGN KEY (`device_type_id`) REFERENCES `device_types` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `FK6l2fhb3bt8wpvl5ppp5rvi26t` FOREIGN KEY (`actuating_function_id`) REFERENCES `actuating_functions` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 13 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for actuating_functions
-- ----------------------------
DROP TABLE IF EXISTS `actuating_functions`;
CREATE TABLE `actuating_functions`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `params` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 13 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for adjacent_space
-- ----------------------------
DROP TABLE IF EXISTS `adjacent_space`;
CREATE TABLE `adjacent_space`  (
  `space_id` int NOT NULL,
  `adjacent_space_id` int NOT NULL,
  PRIMARY KEY (`space_id`, `adjacent_space_id`) USING BTREE,
  INDEX `FKeh9j5gqinymp97biup5drocs`(`adjacent_space_id` ASC) USING BTREE,
  CONSTRAINT `FK1b1a5vuyr4ltcsxrtqs6xgxhh` FOREIGN KEY (`space_id`) REFERENCES `spaces` (`space_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `FKeh9j5gqinymp97biup5drocs` FOREIGN KEY (`adjacent_space_id`) REFERENCES `spaces` (`space_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for app_grid
-- ----------------------------
DROP TABLE IF EXISTS `app_grid`;
CREATE TABLE `app_grid`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `grid_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `app_rule_id` int NOT NULL,
  `enabled` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否启用：1启用，0禁用',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 367 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for app_rule_info
-- ----------------------------
DROP TABLE IF EXISTS `app_rule_info`;
CREATE TABLE `app_rule_info`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `description` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `rule_json` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `update_time` datetime(6) NULL DEFAULT NULL,
  `project_id` int NULL DEFAULT NULL,
  `event_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '事件类型',
  `flow_json` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `app_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '应用名称',
  `cross_region` bit(1) NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `FK77medpkys1ptrynem23ctmy1k`(`project_id` ASC) USING BTREE,
  CONSTRAINT `FK77medpkys1ptrynem23ctmy1k` FOREIGN KEY (`project_id`) REFERENCES `projects` (`project_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 368 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for app_rule_log
-- ----------------------------
DROP TABLE IF EXISTS `app_rule_log`;
CREATE TABLE `app_rule_log`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `event_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `wait_value` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `logs` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `timestamp` datetime(6) NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 419 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for city_mesh_grid
-- ----------------------------
DROP TABLE IF EXISTS `city_mesh_grid`;
CREATE TABLE `city_mesh_grid`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `mesh_id` int NULL DEFAULT NULL,
  `x` double NULL DEFAULT NULL,
  `y` double NULL DEFAULT NULL,
  `z` double NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `mesh_id`(`mesh_id` ASC) USING BTREE,
  CONSTRAINT `city_mesh_grid_ibfk_1` FOREIGN KEY (`mesh_id`) REFERENCES `city_mesh_info` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1861 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for city_mesh_info
-- ----------------------------
DROP TABLE IF EXISTS `city_mesh_info`;
CREATE TABLE `city_mesh_info`  (
  `id` int NOT NULL,
  `mesh_code` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `mesh_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `mesh_type` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `is_businessdistrict` tinyint(1) NOT NULL DEFAULT 0,
  `is_residential` tinyint(1) NOT NULL DEFAULT 0,
  `is_other` tinyint(1) NOT NULL DEFAULT 0,
  `is_mainroad` tinyint(1) NOT NULL DEFAULT 0,
  `neighbor_ids` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for cyber_resources
-- ----------------------------
DROP TABLE IF EXISTS `cyber_resources`;
CREATE TABLE `cyber_resources`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `details` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `last_update_time` datetime(6) NULL DEFAULT NULL,
  `resource_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `resource_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `state` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `project_id` int NULL DEFAULT NULL,
  `operatorHttpMethod` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'POST',
  `output` json NULL,
  `input` json NULL,
  `url` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `UK7jomhre4c251m41pu2rxiakde`(`project_id` ASC, `resource_id` ASC) USING BTREE,
  CONSTRAINT `FKra0ca6v92mkhch6fttm9bf8ct` FOREIGN KEY (`project_id`) REFERENCES `projects` (`project_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 18 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for data_event_history
-- ----------------------------
DROP TABLE IF EXISTS `data_event_history`;
CREATE TABLE `data_event_history`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `event_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `event_source` enum('sensorEvent','spaceEvent') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `payload` json NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 579 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for datasource_type
-- ----------------------------
DROP TABLE IF EXISTS `datasource_type`;
CREATE TABLE `datasource_type`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `datasource_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 12 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for device_configuration
-- ----------------------------
DROP TABLE IF EXISTS `device_configuration`;
CREATE TABLE `device_configuration`  (
  `device_id` int NOT NULL AUTO_INCREMENT,
  `configuration` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `device_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `lha` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  PRIMARY KEY (`device_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for device_types
-- ----------------------------
DROP TABLE IF EXISTS `device_types`;
CREATE TABLE `device_types`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `device_type_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `device_type_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `is_sensor` bit(1) NOT NULL,
  `project_id` int NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `UK8gv5ewg7ogo5e6c686384xb8s`(`project_id` ASC, `device_type_id` ASC) USING BTREE,
  CONSTRAINT `FK1029aga0222386vn84c5onkqg` FOREIGN KEY (`project_id`) REFERENCES `projects` (`project_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 13 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for devices
-- ----------------------------
DROP TABLE IF EXISTS `devices`;
CREATE TABLE `devices`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `coordinatex` float NULL DEFAULT NULL,
  `coordinatey` float NULL DEFAULT NULL,
  `coordinatez` float NULL DEFAULT NULL,
  `device_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `device_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `fixed_properties` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `last_update_time` datetime(6) NULL DEFAULT NULL,
  `device_type_id` int NULL DEFAULT NULL,
  `space_id` int NULL DEFAULT NULL,
  `project_id` int NOT NULL DEFAULT 1,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `UK7xojrvo11or0ulnbhl1eo5l2h`(`space_id` ASC, `device_id` ASC) USING BTREE,
  INDEX `FKthsup9yv35eehh6hkt0jj3naw`(`device_type_id` ASC) USING BTREE,
  CONSTRAINT `fk_devices_space` FOREIGN KEY (`space_id`) REFERENCES `spaces` (`space_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `FKthsup9yv35eehh6hkt0jj3naw` FOREIGN KEY (`device_type_id`) REFERENCES `device_types` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 32 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for edge_node
-- ----------------------------
DROP TABLE IF EXISTS `edge_node`;
CREATE TABLE `edge_node`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `grid_id` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '边缘节点网格id',
  `ip_address` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '边缘节点的IP地址',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `grid_id`(`grid_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '边缘节点信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for env_event
-- ----------------------------
DROP TABLE IF EXISTS `env_event`;
CREATE TABLE `env_event`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `event_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `event_json` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `rule_dsl` json NULL,
  `event_name` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `cross_region` bit(1) NOT NULL,
  `create_time` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  `depend_dtypes` json NULL,
  `project_id` int NULL DEFAULT 1,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 107 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for env_event_grid
-- ----------------------------
DROP TABLE IF EXISTS `env_event_grid`;
CREATE TABLE `env_event_grid`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `grid_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `env_event_id` int NOT NULL,
  `enabled` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否启用：1启用，0禁用',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 320 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for env_property
-- ----------------------------
DROP TABLE IF EXISTS `env_property`;
CREATE TABLE `env_property`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `property_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 21 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for env_service
-- ----------------------------
DROP TABLE IF EXISTS `env_service`;
CREATE TABLE `env_service`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `service_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `service_json` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `rule_json` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `cross_region` bit(1) NOT NULL,
  `create_time` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `depend_dtypes` json NULL,
  `project_id` int NULL DEFAULT 1,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 182 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for env_service_grid
-- ----------------------------
DROP TABLE IF EXISTS `env_service_grid`;
CREATE TABLE `env_service_grid`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `grid_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `env_service_id` int NOT NULL,
  `enabled` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否启用：1启用，0禁用',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 338 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for env_services
-- ----------------------------
DROP TABLE IF EXISTS `env_services`;
CREATE TABLE `env_services`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `env_service_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `project_id` int NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `FKrleasr5xapa7p15cefkow7sat`(`project_id` ASC) USING BTREE,
  CONSTRAINT `FKrleasr5xapa7p15cefkow7sat` FOREIGN KEY (`project_id`) REFERENCES `projects` (`project_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 18 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for event_fusion_rule_entity
-- ----------------------------
DROP TABLE IF EXISTS `event_fusion_rule_entity`;
CREATE TABLE `event_fusion_rule_entity`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `event_fusion_rule` json NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for event_fusion_run_history
-- ----------------------------
DROP TABLE IF EXISTS `event_fusion_run_history`;
CREATE TABLE `event_fusion_run_history`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `logs` json NULL,
  `published_event` json NULL,
  `rule_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `step_outputs` json NULL,
  `triggers` json NULL,
  `is_success` bit(1) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 588 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for event_history
-- ----------------------------
DROP TABLE IF EXISTS `event_history`;
CREATE TABLE `event_history`  (
  `history_id` int NOT NULL AUTO_INCREMENT,
  `data_value` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `event_data` tinytext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `event_details` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `event_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `location` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `object_id` int NULL DEFAULT NULL,
  `state` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `timestamp` datetime(6) NOT NULL,
  PRIMARY KEY (`history_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1044 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for events
-- ----------------------------
DROP TABLE IF EXISTS `events`;
CREATE TABLE `events`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `event_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `event_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `space_id` int NULL DEFAULT NULL,
  `property_id` int NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `UKpj4dwhrvv0757mldcpu2qjdhs`(`space_id` ASC, `event_id` ASC) USING BTREE,
  INDEX `FKqhuovtrt3aiat70819smcrxny`(`property_id` ASC) USING BTREE,
  CONSTRAINT `fk_events_space` FOREIGN KEY (`space_id`) REFERENCES `spaces` (`space_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `FKqhuovtrt3aiat70819smcrxny` FOREIGN KEY (`property_id`) REFERENCES `properties` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for fusion_rule
-- ----------------------------
DROP TABLE IF EXISTS `fusion_rule`;
CREATE TABLE `fusion_rule`  (
  `rule_id` int NOT NULL AUTO_INCREMENT,
  `rule_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `project_id` int NULL DEFAULT NULL,
  `flow_json` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `fusion_target` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `rule_json` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `status` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`rule_id`) USING BTREE,
  INDEX `FKfhkh16r4rluu07pu3vq9bwyip`(`project_id` ASC) USING BTREE,
  CONSTRAINT `FKfhkh16r4rluu07pu3vq9bwyip` FOREIGN KEY (`project_id`) REFERENCES `projects` (`project_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 103 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for fusion_rule_branch
-- ----------------------------
DROP TABLE IF EXISTS `fusion_rule_branch`;
CREATE TABLE `fusion_rule_branch`  (
  `branch_id` int NOT NULL AUTO_INCREMENT,
  `rule_id` int NOT NULL,
  `branch_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `fusion_target` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'inactive',
  `rule_json` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `flow_json` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `remark` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `space_id` int NULL DEFAULT NULL,
  `branch_index` int NOT NULL,
  PRIMARY KEY (`branch_id`) USING BTREE,
  UNIQUE INDEX `uk_rule_space_idx`(`rule_id` ASC, `space_id` ASC) USING BTREE,
  INDEX `idx_branch_rule`(`rule_id` ASC) USING BTREE,
  INDEX `idx_branch_space`(`space_id` ASC) USING BTREE,
  CONSTRAINT `fk_branch_rule` FOREIGN KEY (`rule_id`) REFERENCES `fusion_rule` (`rule_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_branch_space` FOREIGN KEY (`space_id`) REFERENCES `spaces` (`space_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 72 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for grid_list
-- ----------------------------
DROP TABLE IF EXISTS `grid_list`;
CREATE TABLE `grid_list`  (
  `id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `mesh_no` varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `mesh_name` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `mesh_nature` varchar(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `mesh_area` double NULL DEFAULT NULL,
  `mesh_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '网格类型'
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for mesh_device_statistics
-- ----------------------------
DROP TABLE IF EXISTS `mesh_device_statistics`;
CREATE TABLE `mesh_device_statistics`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `mesh_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `mesh_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `product_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `device_instances_count` int NULL DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 175 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for mesh_info
-- ----------------------------
DROP TABLE IF EXISTS `mesh_info`;
CREATE TABLE `mesh_info`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `mesh_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `mesh_grid_list` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `mesh_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `mesh_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `project_id` int NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 34 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for message_info
-- ----------------------------
DROP TABLE IF EXISTS `message_info`;
CREATE TABLE `message_info`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `create_time` datetime(6) NULL DEFAULT NULL,
  `message_type` int NOT NULL,
  `uuid` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for operator
-- ----------------------------
DROP TABLE IF EXISTS `operator`;
CREATE TABLE `operator`  (
  `operator_id` int NOT NULL AUTO_INCREMENT,
  `operator_api` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `operator_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `output_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `required_input` bit(1) NULL DEFAULT NULL,
  PRIMARY KEY (`operator_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for person
-- ----------------------------
DROP TABLE IF EXISTS `person`;
CREATE TABLE `person`  (
  `person_id` int NOT NULL AUTO_INCREMENT,
  `person_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `space_id` int NULL DEFAULT NULL,
  PRIMARY KEY (`person_id`) USING BTREE,
  INDEX `fk_person_space`(`space_id` ASC) USING BTREE,
  CONSTRAINT `fk_person_space` FOREIGN KEY (`space_id`) REFERENCES `spaces` (`space_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for product_command_json
-- ----------------------------
DROP TABLE IF EXISTS `product_command_json`;
CREATE TABLE `product_command_json`  (
  `product_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `command_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `command_json` json NULL,
  PRIMARY KEY (`product_id`, `command_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for product_event
-- ----------------------------
DROP TABLE IF EXISTS `product_event`;
CREATE TABLE `product_event`  (
  `product_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `product_event` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `event_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`product_id`, `product_event`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for product_event_json
-- ----------------------------
DROP TABLE IF EXISTS `product_event_json`;
CREATE TABLE `product_event_json`  (
  `product_event` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `event_json` json NULL,
  `event_format` json NULL,
  PRIMARY KEY (`product_event`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for product_function_command
-- ----------------------------
DROP TABLE IF EXISTS `product_function_command`;
CREATE TABLE `product_function_command`  (
  `product_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `function_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `function_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `command_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `command_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`product_id`, `command_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for projects
-- ----------------------------
DROP TABLE IF EXISTS `projects`;
CREATE TABLE `projects`  (
  `project_id` int NOT NULL AUTO_INCREMENT,
  `project_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `thumbnail` tinyblob NULL,
  PRIMARY KEY (`project_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 15 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for properties
-- ----------------------------
DROP TABLE IF EXISTS `properties`;
CREATE TABLE `properties`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `property_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `property_key` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `project_id` int NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `UKckprmywbiihwe9fs35yxl3efl`(`project_id` ASC, `property_id` ASC) USING BTREE,
  CONSTRAINT `FKev1rvgy8cdnmwc94rub8go6fc` FOREIGN KEY (`project_id`) REFERENCES `projects` (`project_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for property_space
-- ----------------------------
DROP TABLE IF EXISTS `property_space`;
CREATE TABLE `property_space`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `property_value` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `property_id` int NOT NULL,
  `space_id` int NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `FK270t2orrsbwleooyb6ygvgmj8`(`property_id` ASC) USING BTREE,
  INDEX `FK4k75vv2i0rr6lygcaxnjeq6j7`(`space_id` ASC) USING BTREE,
  CONSTRAINT `FK270t2orrsbwleooyb6ygvgmj8` FOREIGN KEY (`property_id`) REFERENCES `properties` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_property_space` FOREIGN KEY (`space_id`) REFERENCES `spaces` (`space_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for services
-- ----------------------------
DROP TABLE IF EXISTS `services`;
CREATE TABLE `services`  (
  `service_id` int NOT NULL AUTO_INCREMENT,
  `service_des` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `project_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `service_json` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `service_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `service_csp` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `space_id` int NULL DEFAULT NULL,
  PRIMARY KEY (`service_id`) USING BTREE,
  UNIQUE INDEX `UKb6sj2j2g2ebaex8qj7rkvblin`(`space_id` ASC, `service_id` ASC) USING BTREE,
  CONSTRAINT `FKi43m8x8klr2cis39718twhlcn` FOREIGN KEY (`space_id`) REFERENCES `spaces` (`space_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 24 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for social_resources
-- ----------------------------
DROP TABLE IF EXISTS `social_resources`;
CREATE TABLE `social_resources`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `details` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `last_update_time` datetime(6) NULL DEFAULT NULL,
  `resource_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `resource_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `state` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `url` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `project_id` int NULL DEFAULT NULL,
  `input` json NULL,
  `output` json NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `UKoqn9qidnsc3unf5mm0gikcsbm`(`project_id` ASC, `resource_id` ASC) USING BTREE,
  CONSTRAINT `FK7l37ekvhcu9tli4q6qtsoj0qh` FOREIGN KEY (`project_id`) REFERENCES `projects` (`project_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for space_event_history
-- ----------------------------
DROP TABLE IF EXISTS `space_event_history`;
CREATE TABLE `space_event_history`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `payload` json NULL,
  `space_event_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 88 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for spaces
-- ----------------------------
DROP TABLE IF EXISTS `spaces`;
CREATE TABLE `spaces`  (
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `fixed_properties` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `space_id` int NOT NULL AUTO_INCREMENT,
  `space_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `project_id` int NULL DEFAULT NULL,
  PRIMARY KEY (`space_id`) USING BTREE,
  UNIQUE INDEX `UKnr2vtu7sdu3net3xffyfu4vm7`(`project_id` ASC, `space_id` ASC) USING BTREE,
  CONSTRAINT `FKb1t1p9hs1cdrvwpu8qdv5yfkb` FOREIGN KEY (`project_id`) REFERENCES `projects` (`project_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 14 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for state_device_types
-- ----------------------------
DROP TABLE IF EXISTS `state_device_types`;
CREATE TABLE `state_device_types`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `device_type_id` int NOT NULL,
  `state_id` int NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `FKkybmiuef9p98a693xf4jhhqpr`(`device_type_id` ASC) USING BTREE,
  INDEX `FKgfb8lswjobgpvv3dn2rlb2aip`(`state_id` ASC) USING BTREE,
  CONSTRAINT `FKgfb8lswjobgpvv3dn2rlb2aip` FOREIGN KEY (`state_id`) REFERENCES `states` (`state_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `FKkybmiuef9p98a693xf4jhhqpr` FOREIGN KEY (`device_type_id`) REFERENCES `device_types` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for state_devices
-- ----------------------------
DROP TABLE IF EXISTS `state_devices`;
CREATE TABLE `state_devices`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `state_value` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `device_id` int NOT NULL,
  `state_id` int NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `FKoipnti0xck5wtje11at2otbla`(`device_id` ASC) USING BTREE,
  INDEX `FK52yr1kwe9f94ty0k36omliobr`(`state_id` ASC) USING BTREE,
  CONSTRAINT `FK52yr1kwe9f94ty0k36omliobr` FOREIGN KEY (`state_id`) REFERENCES `states` (`state_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `FKoipnti0xck5wtje11at2otbla` FOREIGN KEY (`device_id`) REFERENCES `devices` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for states
-- ----------------------------
DROP TABLE IF EXISTS `states`;
CREATE TABLE `states`  (
  `state_id` int NOT NULL AUTO_INCREMENT,
  `state_key` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  PRIMARY KEY (`state_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for tsl_devices
-- ----------------------------
DROP TABLE IF EXISTS `tsl_devices`;
CREATE TABLE `tsl_devices`  (
  `id` int NOT NULL,
  `project_id` bigint NULL DEFAULT NULL,
  `device_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `device_id` bigint NULL DEFAULT NULL,
  `product_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `status` int NULL DEFAULT NULL,
  `mesh_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `mesh_no` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `mesh_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `mesh_nature` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `mesh_area` double NULL DEFAULT NULL,
  `address` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `created_at` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `FKr8agoukyghqpuy25r8sxb2pkv`(`product_id` ASC) USING BTREE,
  CONSTRAINT `FKr8agoukyghqpuy25r8sxb2pkv` FOREIGN KEY (`product_id`) REFERENCES `tsl_product` (`product_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for tsl_product
-- ----------------------------
DROP TABLE IF EXISTS `tsl_product`;
CREATE TABLE `tsl_product`  (
  `product_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `product_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `product_describe` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `product_function` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `product_property` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `product_instruction` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `product_event` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `product_json` json NULL,
  `action_name` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `project_id` int NULL DEFAULT NULL,
  PRIMARY KEY (`product_id`) USING BTREE,
  UNIQUE INDEX `UKmxhdbriuu3ae2wlcq7nx7wnnr`(`project_id` ASC) USING BTREE,
  CONSTRAINT `FK7fwrvfm1dv5foqenmfxxdrwjf` FOREIGN KEY (`project_id`) REFERENCES `projects` (`project_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for user_project
-- ----------------------------
DROP TABLE IF EXISTS `user_project`;
CREATE TABLE `user_project`  (
  `user_id` int NOT NULL,
  `project_id` int NOT NULL,
  PRIMARY KEY (`user_id`, `project_id`) USING BTREE,
  INDEX `FKc74un5y8u03pxfbvjdvm3kg06`(`project_id` ASC) USING BTREE,
  CONSTRAINT `FKc74un5y8u03pxfbvjdvm3kg06` FOREIGN KEY (`project_id`) REFERENCES `projects` (`project_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `FKjoreo8pojddvrp3cr4x8b610b` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for users
-- ----------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users`  (
  `user_id` int NOT NULL AUTO_INCREMENT,
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `username` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`user_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

SET FOREIGN_KEY_CHECKS = 1;
