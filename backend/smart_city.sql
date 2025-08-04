/*
 Navicat Premium Data Transfer

 Source Server         : 10.192.48.114
 Source Server Type    : MySQL
 Source Server Version : 80042 (8.0.42-0ubuntu0.22.04.1)
 Source Host           : 10.192.48.114:3306
 Source Schema         : smart_city

 Target Server Type    : MySQL
 Target Server Version : 80042 (8.0.42-0ubuntu0.22.04.1)
 File Encoding         : 65001

 Date: 31/07/2025 11:34:50
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
) ENGINE = InnoDB AUTO_INCREMENT = 14 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

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
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

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
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for adjacent_space
-- ----------------------------
DROP TABLE IF EXISTS `adjacent_space`;
CREATE TABLE `adjacent_space`  (
  `space_id` int NOT NULL,
  `adjacent_space_id` int NOT NULL,
  PRIMARY KEY (`space_id`, `adjacent_space_id`) USING BTREE,
  INDEX `FKeh9j5gqinymp97biup5drocs`(`adjacent_space_id` ASC) USING BTREE,
  CONSTRAINT `fk_adjacent_space_adjacent` FOREIGN KEY (`adjacent_space_id`) REFERENCES `spaces` (`space_id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_adjacent_space_main` FOREIGN KEY (`space_id`) REFERENCES `spaces` (`space_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for app_rule_info
-- ----------------------------
DROP TABLE IF EXISTS `app_rule_info`;
CREATE TABLE `app_rule_info`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `rule_json` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `update_time` datetime(6) NULL DEFAULT NULL,
  `project_id` int NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `FK77medpkys1ptrynem23ctmy1k`(`project_id` ASC) USING BTREE,
  CONSTRAINT `FK77medpkys1ptrynem23ctmy1k` FOREIGN KEY (`project_id`) REFERENCES `projects` (`project_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 35 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

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
  `url` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `project_id` int NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `UK7jomhre4c251m41pu2rxiakde`(`project_id` ASC, `resource_id` ASC) USING BTREE,
  CONSTRAINT `FKra0ca6v92mkhch6fttm9bf8ct` FOREIGN KEY (`project_id`) REFERENCES `projects` (`project_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

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
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

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
) ENGINE = InnoDB AUTO_INCREMENT = 12 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

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
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `UK7xojrvo11or0ulnbhl1eo5l2h`(`space_id` ASC, `device_id` ASC) USING BTREE,
  INDEX `FKthsup9yv35eehh6hkt0jj3naw`(`device_type_id` ASC) USING BTREE,
  CONSTRAINT `fk_devices_space` FOREIGN KEY (`space_id`) REFERENCES `spaces` (`space_id`) ON DELETE SET NULL ON UPDATE RESTRICT,
  CONSTRAINT `FKthsup9yv35eehh6hkt0jj3naw` FOREIGN KEY (`device_type_id`) REFERENCES `device_types` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 17 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for event_history
-- ----------------------------
DROP TABLE IF EXISTS `event_history`;
CREATE TABLE `event_history`  (
  `history_id` int NOT NULL AUTO_INCREMENT,
  `data_value` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `event_data` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `event_details` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `event_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `location` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `object_id` int NOT NULL,
  `state` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `timestamp` datetime(6) NOT NULL,
  PRIMARY KEY (`history_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

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
  CONSTRAINT `fk_events_space` FOREIGN KEY (`space_id`) REFERENCES `spaces` (`space_id`) ON DELETE SET NULL ON UPDATE RESTRICT,
  CONSTRAINT `FKqhuovtrt3aiat70819smcrxny` FOREIGN KEY (`property_id`) REFERENCES `properties` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for fusion_rule
-- ----------------------------
DROP TABLE IF EXISTS `fusion_rule`;
CREATE TABLE `fusion_rule`  (
  `rule_id` int NOT NULL AUTO_INCREMENT,
  `flow_json` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `rule_json` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `rule_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `project_id` int NULL DEFAULT NULL,
  `status` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `fusion_target` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`rule_id`) USING BTREE,
  INDEX `FKfhkh16r4rluu07pu3vq9bwyip`(`project_id` ASC) USING BTREE,
  CONSTRAINT `FKfhkh16r4rluu07pu3vq9bwyip` FOREIGN KEY (`project_id`) REFERENCES `projects` (`project_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 61 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

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
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

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
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

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
  CONSTRAINT `fk_person_space` FOREIGN KEY (`space_id`) REFERENCES `spaces` (`space_id`) ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for projects
-- ----------------------------
DROP TABLE IF EXISTS `projects`;
CREATE TABLE `projects`  (
  `project_id` int NOT NULL AUTO_INCREMENT,
  `project_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `thumbnail` tinyblob NULL,
  PRIMARY KEY (`project_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

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
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

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
  CONSTRAINT `fk_property_space` FOREIGN KEY (`space_id`) REFERENCES `spaces` (`space_id`) ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for services
-- ----------------------------
DROP TABLE IF EXISTS `services`;
CREATE TABLE `services`  (
  `service_id` int NOT NULL AUTO_INCREMENT,
  `project_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `service_csp` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `service_json` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `service_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `space_id` int NULL DEFAULT NULL,
  `service_des` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`service_id`) USING BTREE,
  UNIQUE INDEX `UKb6sj2j2g2ebaex8qj7rkvblin`(`space_id` ASC, `service_id` ASC) USING BTREE,
  CONSTRAINT `fk_services_space` FOREIGN KEY (`space_id`) REFERENCES `spaces` (`space_id`) ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

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
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `UKoqn9qidnsc3unf5mm0gikcsbm`(`project_id` ASC, `resource_id` ASC) USING BTREE,
  CONSTRAINT `FK7l37ekvhcu9tli4q6qtsoj0qh` FOREIGN KEY (`project_id`) REFERENCES `projects` (`project_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for spaces
-- ----------------------------
DROP TABLE IF EXISTS `spaces`;
CREATE TABLE `spaces`  (
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `fixed_properties` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `space_id` int NOT NULL,
  `space_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `project_id` int NULL DEFAULT NULL,
  PRIMARY KEY (`space_id`) USING BTREE,
  UNIQUE INDEX `UKnr2vtu7sdu3net3xffyfu4vm7`(`project_id` ASC, `space_id` ASC) USING BTREE,
  CONSTRAINT `FKb1t1p9hs1cdrvwpu8qdv5yfkb` FOREIGN KEY (`project_id`) REFERENCES `projects` (`project_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

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
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

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
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for states
-- ----------------------------
DROP TABLE IF EXISTS `states`;
CREATE TABLE `states`  (
  `state_id` int NOT NULL AUTO_INCREMENT,
  `state_key` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  PRIMARY KEY (`state_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

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
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for users
-- ----------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users`  (
  `user_id` int NOT NULL AUTO_INCREMENT,
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `username` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`user_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
