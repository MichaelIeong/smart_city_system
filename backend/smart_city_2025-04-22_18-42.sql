-- MySQL dump 10.13  Distrib 8.0.41, for Linux (x86_64)
--
-- Host: localhost    Database: smart_city
-- ------------------------------------------------------
-- Server version	8.0.41-0ubuntu0.22.04.1

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `actuating_function_device`
--

DROP TABLE IF EXISTS `actuating_function_device`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `actuating_function_device` (
  `id` int NOT NULL AUTO_INCREMENT,
  `url` varchar(255) DEFAULT NULL,
  `actuating_function_id` int NOT NULL,
  `device_id` int NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKabnu1foahdw7x84s828rv7ugc` (`actuating_function_id`),
  KEY `FK1njd15amieyct3hjmglv7con7` (`device_id`),
  CONSTRAINT `FK1njd15amieyct3hjmglv7con7` FOREIGN KEY (`device_id`) REFERENCES `devices` (`id`),
  CONSTRAINT `FKabnu1foahdw7x84s828rv7ugc` FOREIGN KEY (`actuating_function_id`) REFERENCES `actuating_functions` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `actuating_function_device`
--

LOCK TABLES `actuating_function_device` WRITE;
/*!40000 ALTER TABLE `actuating_function_device` DISABLE KEYS */;
INSERT INTO `actuating_function_device` VALUES (1,NULL,1,1),(2,NULL,1,2),(3,NULL,2,3),(4,NULL,2,4),(5,NULL,3,5),(6,NULL,4,5),(7,NULL,5,6),(8,'api/robot/move',6,8),(9,'api/door/open',7,9),(10,NULL,8,10),(11,NULL,8,11);
/*!40000 ALTER TABLE `actuating_function_device` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `actuating_function_device_type`
--

DROP TABLE IF EXISTS `actuating_function_device_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `actuating_function_device_type` (
  `id` int NOT NULL AUTO_INCREMENT,
  `actuating_function_id` int NOT NULL,
  `device_type_id` int NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK6l2fhb3bt8wpvl5ppp5rvi26t` (`actuating_function_id`),
  KEY `FK2c25pm5d5s86dt999m3ib2umv` (`device_type_id`),
  CONSTRAINT `FK2c25pm5d5s86dt999m3ib2umv` FOREIGN KEY (`device_type_id`) REFERENCES `device_types` (`id`),
  CONSTRAINT `FK6l2fhb3bt8wpvl5ppp5rvi26t` FOREIGN KEY (`actuating_function_id`) REFERENCES `actuating_functions` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `actuating_function_device_type`
--

LOCK TABLES `actuating_function_device_type` WRITE;
/*!40000 ALTER TABLE `actuating_function_device_type` DISABLE KEYS */;
/*!40000 ALTER TABLE `actuating_function_device_type` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `actuating_functions`
--

DROP TABLE IF EXISTS `actuating_functions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `actuating_functions` (
  `id` int NOT NULL AUTO_INCREMENT,
  `description` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `params` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `actuating_functions`
--

LOCK TABLES `actuating_functions` WRITE;
/*!40000 ALTER TABLE `actuating_functions` DISABLE KEYS */;
INSERT INTO `actuating_functions` VALUES (1,'stream','stream',NULL),(2,'temperature detection','temperature','2'),(3,'CO2 detection','CO2 detection',NULL),(4,'CO detection','CO detection',NULL),(5,'humidity','humidity',NULL),(6,'行动','行动',NULL),(7,'开门','开门',NULL),(8,'音量采集','Volume Caption',NULL),(9,'开电视','Turn on TV',NULL);
/*!40000 ALTER TABLE `actuating_functions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `adjacent_space`
--

DROP TABLE IF EXISTS `adjacent_space`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `adjacent_space` (
  `space_id` int NOT NULL,
  `adjacent_space_id` int NOT NULL,
  PRIMARY KEY (`space_id`,`adjacent_space_id`),
  KEY `FKeh9j5gqinymp97biup5drocs` (`adjacent_space_id`),
  CONSTRAINT `FK1b1a5vuyr4ltcsxrtqs6xgxhh` FOREIGN KEY (`space_id`) REFERENCES `spaces` (`id`),
  CONSTRAINT `FKeh9j5gqinymp97biup5drocs` FOREIGN KEY (`adjacent_space_id`) REFERENCES `spaces` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `adjacent_space`
--

LOCK TABLES `adjacent_space` WRITE;
/*!40000 ALTER TABLE `adjacent_space` DISABLE KEYS */;
/*!40000 ALTER TABLE `adjacent_space` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `app_rule_info`
--

DROP TABLE IF EXISTS `app_rule_info`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `app_rule_info` (
  `id` int NOT NULL AUTO_INCREMENT,
  `description` varchar(255) DEFAULT NULL,
  `rule_json` longtext,
  `update_time` datetime(6) DEFAULT NULL,
  `project_id` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK77medpkys1ptrynem23ctmy1k` (`project_id`),
  CONSTRAINT `FK77medpkys1ptrynem23ctmy1k` FOREIGN KEY (`project_id`) REFERENCES `projects` (`project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `app_rule_info`
--

LOCK TABLES `app_rule_info` WRITE;
/*!40000 ALTER TABLE `app_rule_info` DISABLE KEYS */;
/*!40000 ALTER TABLE `app_rule_info` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cyber_resources`
--

DROP TABLE IF EXISTS `cyber_resources`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cyber_resources` (
  `id` int NOT NULL AUTO_INCREMENT,
  `description` varchar(255) DEFAULT NULL,
  `details` varchar(255) DEFAULT NULL,
  `last_update_time` datetime(6) DEFAULT NULL,
  `resource_id` varchar(255) NOT NULL,
  `resource_type` varchar(255) DEFAULT NULL,
  `state` varchar(255) DEFAULT NULL,
  `url` varchar(512) NOT NULL,
  `project_id` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK7jomhre4c251m41pu2rxiakde` (`project_id`,`resource_id`),
  CONSTRAINT `FKra0ca6v92mkhch6fttm9bf8ct` FOREIGN KEY (`project_id`) REFERENCES `projects` (`project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cyber_resources`
--

LOCK TABLES `cyber_resources` WRITE;
/*!40000 ALTER TABLE `cyber_resources` DISABLE KEYS */;
/*!40000 ALTER TABLE `cyber_resources` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `device_configuration`
--

DROP TABLE IF EXISTS `device_configuration`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `device_configuration` (
  `device_id` int NOT NULL AUTO_INCREMENT,
  `configuration` text,
  `device_name` varchar(255) DEFAULT NULL,
  `lha` text,
  PRIMARY KEY (`device_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `device_configuration`
--

LOCK TABLES `device_configuration` WRITE;
/*!40000 ALTER TABLE `device_configuration` DISABLE KEYS */;
/*!40000 ALTER TABLE `device_configuration` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `device_types`
--

DROP TABLE IF EXISTS `device_types`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `device_types` (
  `id` int NOT NULL AUTO_INCREMENT,
  `device_type_id` varchar(255) NOT NULL,
  `device_type_name` varchar(255) NOT NULL,
  `is_sensor` bit(1) NOT NULL,
  `project_id` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK8gv5ewg7ogo5e6c686384xb8s` (`project_id`,`device_type_id`),
  CONSTRAINT `FK1029aga0222386vn84c5onkqg` FOREIGN KEY (`project_id`) REFERENCES `projects` (`project_id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `device_types`
--

LOCK TABLES `device_types` WRITE;
/*!40000 ALTER TABLE `device_types` DISABLE KEYS */;
INSERT INTO `device_types` VALUES (1,'1','摄像头',_binary '',1),(2,'2','温度传感器',_binary '',1),(3,'3','CO/CO2传感器',_binary '',1),(4,'4','湿度传感器',_binary '',1),(5,'5','广播喇叭',_binary '\0',1),(6,'6','机器人',_binary '',1),(7,'7','门',_binary '',1),(8,'8','Microphone',_binary '',1),(9,'9','TV',_binary '\0',1);
/*!40000 ALTER TABLE `device_types` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `devices`
--

DROP TABLE IF EXISTS `devices`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `devices` (
  `id` int NOT NULL AUTO_INCREMENT,
  `coordinatex` float DEFAULT NULL,
  `coordinatey` float DEFAULT NULL,
  `coordinatez` float DEFAULT NULL,
  `device_id` varchar(255) NOT NULL,
  `device_name` varchar(255) NOT NULL,
  `fixed_properties` varchar(255) DEFAULT NULL,
  `last_update_time` datetime(6) DEFAULT NULL,
  `device_type_id` int DEFAULT NULL,
  `space_id` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK7xojrvo11or0ulnbhl1eo5l2h` (`space_id`,`device_id`),
  KEY `FKthsup9yv35eehh6hkt0jj3naw` (`device_type_id`),
  CONSTRAINT `FKqifx9avysps3rcvy7jwynxx5k` FOREIGN KEY (`space_id`) REFERENCES `spaces` (`id`),
  CONSTRAINT `FKthsup9yv35eehh6hkt0jj3naw` FOREIGN KEY (`device_type_id`) REFERENCES `device_types` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `devices`
--

LOCK TABLES `devices` WRITE;
/*!40000 ALTER TABLE `devices` DISABLE KEYS */;
INSERT INTO `devices` VALUES (1,55,55,55,'1','摄像头1','55',NULL,1,1),(2,2,4,5,'2','摄像头2','55',NULL,1,2),(3,5,11,11,'3','温度传感器1','55',NULL,2,1),(4,55,2,33,'4','温度传感器2','55',NULL,2,2),(5,11,55,11,'5','CO/CO2传感器','55',NULL,3,2),(6,55,11,44,'6','湿度传感器','55',NULL,4,2),(7,545,44,3,'7','广播喇叭','55',NULL,5,2),(8,22,33,55,'8','机器人1',NULL,NULL,6,1),(9,22,33,55,'9','智能门1',NULL,NULL,7,1),(10,NULL,NULL,NULL,'10','Microphone','位于会议室',NULL,8,3),(11,NULL,NULL,NULL,'11','Microphone','位于办公室',NULL,8,1),(12,NULL,NULL,NULL,'12','TV','位于会议室',NULL,9,3),(13,NULL,NULL,NULL,'13','TV','位于办公室',NULL,9,1);
/*!40000 ALTER TABLE `devices` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `event_history`
--

DROP TABLE IF EXISTS `event_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `event_history` (
  `history_id` int NOT NULL AUTO_INCREMENT,
  `data_value` varchar(255) DEFAULT NULL,
  `event_data` varchar(255) DEFAULT NULL,
  `event_details` varchar(255) DEFAULT NULL,
  `event_type` varchar(255) NOT NULL,
  `location` varchar(255) NOT NULL,
  `object_id` int NOT NULL,
  `state` varchar(255) DEFAULT NULL,
  `timestamp` datetime(6) NOT NULL,
  PRIMARY KEY (`history_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `event_history`
--

LOCK TABLES `event_history` WRITE;
/*!40000 ALTER TABLE `event_history` DISABLE KEYS */;
/*!40000 ALTER TABLE `event_history` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `events`
--

DROP TABLE IF EXISTS `events`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `events` (
  `id` int NOT NULL AUTO_INCREMENT,
  `event_id` varchar(255) NOT NULL,
  `event_type` varchar(255) NOT NULL,
  `space_id` int DEFAULT NULL,
  `property_id` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKpj4dwhrvv0757mldcpu2qjdhs` (`space_id`,`event_id`),
  KEY `FKqhuovtrt3aiat70819smcrxny` (`property_id`),
  CONSTRAINT `FKqhuovtrt3aiat70819smcrxny` FOREIGN KEY (`property_id`) REFERENCES `properties` (`id`),
  CONSTRAINT `FKsu1acuyf3cj17klowgxvte1x9` FOREIGN KEY (`space_id`) REFERENCES `spaces` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `events`
--

LOCK TABLES `events` WRITE;
/*!40000 ALTER TABLE `events` DISABLE KEYS */;
/*!40000 ALTER TABLE `events` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `fusion_rule`
--

DROP TABLE IF EXISTS `fusion_rule`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `fusion_rule` (
  `rule_id` int NOT NULL AUTO_INCREMENT,
  `flow_json` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
  `rule_json` mediumtext,
  `rule_name` varchar(255) DEFAULT NULL,
  `project_id` int DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`rule_id`),
  KEY `FKfhkh16r4rluu07pu3vq9bwyip` (`project_id`),
  CONSTRAINT `FKfhkh16r4rluu07pu3vq9bwyip` FOREIGN KEY (`project_id`) REFERENCES `projects` (`project_id`)
) ENGINE=InnoDB AUTO_INCREMENT=35 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `fusion_rule`
--

LOCK TABLES `fusion_rule` WRITE;
/*!40000 ALTER TABLE `fusion_rule` DISABLE KEYS */;
INSERT INTO `fusion_rule` VALUES (24,'\"[\\n  {\\n    \\\"id\\\": \\\"4b46d8cfdcac1e7a\\\",\\n    \\\"type\\\": \\\"tab\\\",\\n    \\\"label\\\": \\\"Flow 1\\\",\\n    \\\"disabled\\\": false,\\n    \\\"info\\\": \\\"\\\",\\n    \\\"env\\\": []\\n  },\\n  {\\n    \\\"id\\\": \\\"6647f3ca070b28fd\\\",\\n    \\\"type\\\": \\\"Operator\\\",\\n    \\\"z\\\": \\\"4b46d8cfdcac1e7a\\\",\\n    \\\"operatorName\\\": \\\"温度大于20度\\\",\\n    \\\"value\\\": \\\"20\\\",\\n    \\\"operator\\\": \\\"Greater than\\\",\\n    \\\"output\\\": \\\"Boolean\\\",\\n    \\\"x\\\": 720,\\n    \\\"y\\\": 380,\\n    \\\"wires\\\": [\\n      [\\n        \\\"0c53393282d602d7\\\"\\n      ]\\n    ]\\n  },\\n  {\\n    \\\"id\\\": \\\"87e4ec7c57203b0c\\\",\\n    \\\"type\\\": \\\"Sensor\\\",\\n    \\\"z\\\": \\\"4b46d8cfdcac1e7a\\\",\\n    \\\"location\\\": \\\"Ai Park\\\",\\n    \\\"deviceName\\\": \\\"湿度传感器\\\",\\n    \\\"deviceType\\\": null,\\n    \\\"sensingFunction\\\": \\\"humidity\\\",\\n    \\\"sensorId\\\": \\\"6\\\",\\n    \\\"x\\\": 370,\\n    \\\"y\\\": 220,\\n    \\\"wires\\\": [\\n      [\\n        \\\"bd3998b67e605caa\\\"\\n      ]\\n    ]\\n  },\\n  {\\n    \\\"id\\\": \\\"6a6566b5851c475b\\\",\\n    \\\"type\\\": \\\"Sensor\\\",\\n    \\\"z\\\": \\\"4b46d8cfdcac1e7a\\\",\\n    \\\"location\\\": \\\"Ai Park\\\",\\n    \\\"deviceName\\\": \\\"温度传感器2\\\",\\n    \\\"deviceType\\\": null,\\n    \\\"sensingFunction\\\": \\\"temperature\\\",\\n    \\\"sensorId\\\": \\\"4\\\",\\n    \\\"x\\\": 360,\\n    \\\"y\\\": 380,\\n    \\\"wires\\\": [\\n      [\\n        \\\"6647f3ca070b28fd\\\"\\n      ]\\n    ]\\n  },\\n  {\\n    \\\"id\\\": \\\"bd3998b67e605caa\\\",\\n    \\\"type\\\": \\\"Operator\\\",\\n    \\\"z\\\": \\\"4b46d8cfdcac1e7a\\\",\\n    \\\"operatorName\\\": \\\"湿度大于250%\\\",\\n    \\\"value\\\": \\\"50\\\",\\n    \\\"operator\\\": \\\"Greater than\\\",\\n    \\\"output\\\": \\\"Boolean\\\",\\n    \\\"x\\\": 710,\\n    \\\"y\\\": 220,\\n    \\\"wires\\\": [\\n      [\\n        \\\"0c53393282d602d7\\\"\\n      ]\\n    ]\\n  },\\n  {\\n    \\\"id\\\": \\\"6166ba10ad992374\\\",\\n    \\\"type\\\": \\\"Publish\\\",\\n    \\\"z\\\": \\\"4b46d8cfdcac1e7a\\\",\\n    \\\"name\\\": \\\"\\\",\\n    \\\"rulename\\\": \\\"温湿度过高 \\\",\\n    \\\"x\\\": 1560,\\n    \\\"y\\\": 320,\\n    \\\"wires\\\": []\\n  },\\n  {\\n    \\\"id\\\": \\\"0c53393282d602d7\\\",\\n    \\\"type\\\": \\\"Operator\\\",\\n    \\\"z\\\": \\\"4b46d8cfdcac1e7a\\\",\\n    \\\"operatorName\\\": \\\"时序相差不超过20秒\\\",\\n    \\\"value\\\": null,\\n    \\\"operator\\\": \\\"AND\\\",\\n    \\\"output\\\": \\\"Boolean\\\",\\n    \\\"x\\\": 1150,\\n    \\\"y\\\": 300,\\n    \\\"wires\\\": [\\n      [\\n        \\\"6166ba10ad992374\\\"\\n      ]\\n    ]\\n  }\\n]\"','{\"steps\":3,\"0c53393282d602d7\":{\"type\":\"Operator\",\"step\":3,\"value\":null,\"operator\":\"AND\",\"output\":\"Boolean\"},\"bd3998b67e605caa\":{\"type\":\"Operator\",\"step\":2,\"value\":\"50\",\"operator\":\"Greater than\",\"output\":\"Boolean\"},\"87e4ec7c57203b0c\":{\"type\":\"Sensor\",\"step\":1,\"location\":\"Ai Park\",\"sensorId\":\"6\",\"deviceName\":\"湿度传感器\",\"sensingFunction\":\"humidity\"},\"_msgid\":\"b1b49635fa8cc61a\",\"6647f3ca070b28fd\":{\"type\":\"Operator\",\"step\":2,\"value\":\"20\",\"operator\":\"Greater than\",\"output\":\"Boolean\"},\"6a6566b5851c475b\":{\"type\":\"Sensor\",\"step\":1,\"location\":\"Ai Park\",\"sensorId\":\"4\",\"deviceName\":\"温度传感器2\",\"sensingFunction\":\"temperature\"},\"rulename\":\"温湿度过高 \"}','温湿度过高 ',NULL,'inactive'),(34,'\"[\\n  {\\n    \\\"id\\\": \\\"14be675efa1859e9\\\",\\n    \\\"type\\\": \\\"tab\\\",\\n    \\\"label\\\": \\\"Flow 1\\\",\\n    \\\"disabled\\\": false,\\n    \\\"info\\\": \\\"\\\",\\n    \\\"env\\\": []\\n  },\\n  {\\n    \\\"id\\\": \\\"52e432aa281e9809\\\",\\n    \\\"type\\\": \\\"Sensor\\\",\\n    \\\"z\\\": \\\"14be675efa1859e9\\\",\\n    \\\"location\\\": \\\"湾谷办公室\\\",\\n    \\\"deviceName\\\": \\\"Microphone\\\",\\n    \\\"deviceType\\\": null,\\n    \\\"sensingFunction\\\": \\\"Volume Caption\\\",\\n    \\\"sensorId\\\": \\\"11\\\",\\n    \\\"x\\\": 330,\\n    \\\"y\\\": 220,\\n    \\\"wires\\\": [\\n      [\\n        \\\"f1cee09481c55441\\\"\\n      ]\\n    ]\\n  },\\n  {\\n    \\\"id\\\": \\\"f1cee09481c55441\\\",\\n    \\\"type\\\": \\\"Operator\\\",\\n    \\\"z\\\": \\\"14be675efa1859e9\\\",\\n    \\\"operatorName\\\": \\\"大於50\\\",\\n    \\\"value\\\": \\\"50\\\",\\n    \\\"operator\\\": \\\"Greater than\\\",\\n    \\\"output\\\": \\\"Boolean\\\",\\n    \\\"x\\\": 720,\\n    \\\"y\\\": 240,\\n    \\\"wires\\\": [\\n      [\\n        \\\"417cca2d7611818e\\\"\\n      ]\\n    ]\\n  },\\n  {\\n    \\\"id\\\": \\\"417cca2d7611818e\\\",\\n    \\\"type\\\": \\\"Publish\\\",\\n    \\\"z\\\": \\\"14be675efa1859e9\\\",\\n    \\\"name\\\": \\\"\\\",\\n    \\\"rulename\\\": \\\"mic test\\\",\\n    \\\"x\\\": 1150,\\n    \\\"y\\\": 240,\\n    \\\"wires\\\": []\\n  }\\n]\"','{\"steps\":2,\"f1cee09481c55441\":{\"step\":2,\"type\":\"Operator\",\"operator\":\"Greater than\",\"value\":\"50\",\"output\":\"Boolean\",\"dependencies\":[\"52e432aa281e9809\"]},\"52e432aa281e9809\":{\"step\":1,\"type\":\"Sensor\",\"location\":\"湾谷办公室\",\"sensorId\":\"11\",\"deviceName\":\"Microphone\",\"sensingFunction\":\"Volume Caption\"},\"rulename\":\"mic test\"}','mic test',NULL,'active');
/*!40000 ALTER TABLE `fusion_rule` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `operator`
--

DROP TABLE IF EXISTS `operator`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `operator` (
  `operator_id` int NOT NULL AUTO_INCREMENT,
  `operator_api` varchar(255) DEFAULT NULL,
  `operator_name` varchar(255) DEFAULT NULL,
  `output_name` varchar(255) DEFAULT NULL,
  `required_input` bit(1) DEFAULT NULL,
  PRIMARY KEY (`operator_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `operator`
--

LOCK TABLES `operator` WRITE;
/*!40000 ALTER TABLE `operator` DISABLE KEYS */;
INSERT INTO `operator` VALUES (1,'?','human detection','（bollean）检测到有人',_binary '\0'),(2,'?','fire detection','boolean',_binary '\0'),(3,'?','Volume detection','（bollean）检测到声音',_binary '');
/*!40000 ALTER TABLE `operator` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `person`
--

DROP TABLE IF EXISTS `person`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `person` (
  `id` int NOT NULL AUTO_INCREMENT,
  `person_name` varchar(255) NOT NULL,
  `space_id` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKlfcmff28n1ybhiw6npjp9koxo` (`space_id`),
  CONSTRAINT `FKlfcmff28n1ybhiw6npjp9koxo` FOREIGN KEY (`space_id`) REFERENCES `spaces` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `person`
--

LOCK TABLES `person` WRITE;
/*!40000 ALTER TABLE `person` DISABLE KEYS */;
INSERT INTO `person` VALUES (1,'string',NULL),(2,'越子',3),(3,'土子哥',1);
/*!40000 ALTER TABLE `person` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `projects`
--

DROP TABLE IF EXISTS `projects`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `projects` (
  `project_id` int NOT NULL AUTO_INCREMENT,
  `project_name` varchar(255) NOT NULL,
  `thumbnail` tinyblob,
  PRIMARY KEY (`project_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `projects`
--

LOCK TABLES `projects` WRITE;
/*!40000 ALTER TABLE `projects` DISABLE KEYS */;
INSERT INTO `projects` VALUES (1,'Mian Project',NULL);
/*!40000 ALTER TABLE `projects` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `properties`
--

DROP TABLE IF EXISTS `properties`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `properties` (
  `id` int NOT NULL AUTO_INCREMENT,
  `property_id` varchar(255) NOT NULL,
  `property_key` varchar(255) NOT NULL,
  `project_id` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKckprmywbiihwe9fs35yxl3efl` (`project_id`,`property_id`),
  CONSTRAINT `FKev1rvgy8cdnmwc94rub8go6fc` FOREIGN KEY (`project_id`) REFERENCES `projects` (`project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `properties`
--

LOCK TABLES `properties` WRITE;
/*!40000 ALTER TABLE `properties` DISABLE KEYS */;
/*!40000 ALTER TABLE `properties` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `property_space`
--

DROP TABLE IF EXISTS `property_space`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `property_space` (
  `id` int NOT NULL AUTO_INCREMENT,
  `property_value` varchar(255) DEFAULT NULL,
  `property_id` int NOT NULL,
  `space_id` int NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK270t2orrsbwleooyb6ygvgmj8` (`property_id`),
  KEY `FK4k75vv2i0rr6lygcaxnjeq6j7` (`space_id`),
  CONSTRAINT `FK270t2orrsbwleooyb6ygvgmj8` FOREIGN KEY (`property_id`) REFERENCES `properties` (`id`),
  CONSTRAINT `FK4k75vv2i0rr6lygcaxnjeq6j7` FOREIGN KEY (`space_id`) REFERENCES `spaces` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `property_space`
--

LOCK TABLES `property_space` WRITE;
/*!40000 ALTER TABLE `property_space` DISABLE KEYS */;
/*!40000 ALTER TABLE `property_space` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `services`
--

DROP TABLE IF EXISTS `services`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `services` (
  `service_id` int NOT NULL AUTO_INCREMENT,
  `project_id` varchar(255) NOT NULL,
  `service_csp` varchar(255) DEFAULT NULL,
  `service_json` text,
  `service_name` varchar(255) NOT NULL,
  `space_id` int DEFAULT NULL,
  PRIMARY KEY (`service_id`),
  UNIQUE KEY `UKb6sj2j2g2ebaex8qj7rkvblin` (`space_id`,`service_id`),
  CONSTRAINT `FKi43m8x8klr2cis39718twhlcn` FOREIGN KEY (`space_id`) REFERENCES `spaces` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `services`
--

LOCK TABLES `services` WRITE;
/*!40000 ALTER TABLE `services` DISABLE KEYS */;
/*!40000 ALTER TABLE `services` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `social_resources`
--

DROP TABLE IF EXISTS `social_resources`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `social_resources` (
  `id` int NOT NULL AUTO_INCREMENT,
  `description` varchar(255) DEFAULT NULL,
  `details` varchar(255) DEFAULT NULL,
  `last_update_time` datetime(6) DEFAULT NULL,
  `resource_id` varchar(255) NOT NULL,
  `resource_type` varchar(255) DEFAULT NULL,
  `state` varchar(255) DEFAULT NULL,
  `url` varchar(512) NOT NULL,
  `project_id` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKoqn9qidnsc3unf5mm0gikcsbm` (`project_id`,`resource_id`),
  CONSTRAINT `FK7l37ekvhcu9tli4q6qtsoj0qh` FOREIGN KEY (`project_id`) REFERENCES `projects` (`project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `social_resources`
--

LOCK TABLES `social_resources` WRITE;
/*!40000 ALTER TABLE `social_resources` DISABLE KEYS */;
/*!40000 ALTER TABLE `social_resources` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `spaces`
--

DROP TABLE IF EXISTS `spaces`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `spaces` (
  `id` int NOT NULL AUTO_INCREMENT,
  `description` varchar(255) DEFAULT NULL,
  `fixed_properties` varchar(255) DEFAULT NULL,
  `space_id` varchar(255) NOT NULL,
  `space_name` varchar(255) NOT NULL,
  `project_id` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKnr2vtu7sdu3net3xffyfu4vm7` (`project_id`,`space_id`),
  CONSTRAINT `FKb1t1p9hs1cdrvwpu8qdv5yfkb` FOREIGN KEY (`project_id`) REFERENCES `projects` (`project_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `spaces`
--

LOCK TABLES `spaces` WRITE;
/*!40000 ALTER TABLE `spaces` DISABLE KEYS */;
INSERT INTO `spaces` VALUES (1,'湾谷办公室',NULL,'1','湾谷办公室',1),(2,'Ai Park',NULL,'2','Ai Park',1),(3,'湾谷会议室',NULL,'3','湾谷会议室',1);
/*!40000 ALTER TABLE `spaces` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `state_device_types`
--

DROP TABLE IF EXISTS `state_device_types`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `state_device_types` (
  `id` int NOT NULL AUTO_INCREMENT,
  `device_type_id` int NOT NULL,
  `state_id` int NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKkybmiuef9p98a693xf4jhhqpr` (`device_type_id`),
  KEY `FKgfb8lswjobgpvv3dn2rlb2aip` (`state_id`),
  CONSTRAINT `FKgfb8lswjobgpvv3dn2rlb2aip` FOREIGN KEY (`state_id`) REFERENCES `states` (`state_id`),
  CONSTRAINT `FKkybmiuef9p98a693xf4jhhqpr` FOREIGN KEY (`device_type_id`) REFERENCES `device_types` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `state_device_types`
--

LOCK TABLES `state_device_types` WRITE;
/*!40000 ALTER TABLE `state_device_types` DISABLE KEYS */;
/*!40000 ALTER TABLE `state_device_types` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `state_devices`
--

DROP TABLE IF EXISTS `state_devices`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `state_devices` (
  `id` int NOT NULL AUTO_INCREMENT,
  `state_value` varchar(255) DEFAULT NULL,
  `device_id` int NOT NULL,
  `state_id` int NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKoipnti0xck5wtje11at2otbla` (`device_id`),
  KEY `FK52yr1kwe9f94ty0k36omliobr` (`state_id`),
  CONSTRAINT `FK52yr1kwe9f94ty0k36omliobr` FOREIGN KEY (`state_id`) REFERENCES `states` (`state_id`),
  CONSTRAINT `FKoipnti0xck5wtje11at2otbla` FOREIGN KEY (`device_id`) REFERENCES `devices` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `state_devices`
--

LOCK TABLES `state_devices` WRITE;
/*!40000 ALTER TABLE `state_devices` DISABLE KEYS */;
/*!40000 ALTER TABLE `state_devices` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `states`
--

DROP TABLE IF EXISTS `states`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `states` (
  `state_id` int NOT NULL AUTO_INCREMENT,
  `state_key` varchar(255) NOT NULL,
  PRIMARY KEY (`state_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `states`
--

LOCK TABLES `states` WRITE;
/*!40000 ALTER TABLE `states` DISABLE KEYS */;
/*!40000 ALTER TABLE `states` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_project`
--

DROP TABLE IF EXISTS `user_project`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_project` (
  `user_id` int NOT NULL,
  `project_id` int NOT NULL,
  PRIMARY KEY (`user_id`,`project_id`),
  KEY `FKc74un5y8u03pxfbvjdvm3kg06` (`project_id`),
  CONSTRAINT `FKc74un5y8u03pxfbvjdvm3kg06` FOREIGN KEY (`project_id`) REFERENCES `projects` (`project_id`),
  CONSTRAINT `FKjoreo8pojddvrp3cr4x8b610b` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_project`
--

LOCK TABLES `user_project` WRITE;
/*!40000 ALTER TABLE `user_project` DISABLE KEYS */;
/*!40000 ALTER TABLE `user_project` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `user_id` int NOT NULL AUTO_INCREMENT,
  `password` varchar(255) DEFAULT NULL,
  `username` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (1,'$2a$10$r6fD3wa1HBa2Qm.Un9FHweoWC391D/0RR8l8vFlBsaaJQGyvy8sby','lzf'),(2,'$2a$10$Sfdub5iNS4xWlQzlg1sB9eXjGR0ElximAYX4lgwf4Gt04OYgkz.Pe','michael');
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-04-22 18:42:20
