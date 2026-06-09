/*
 Navicat Premium Data Transfer

 Source Server         : MacLocal
 Source Server Type    : MySQL
 Source Server Version : 80043 (8.0.43)
 Source Host           : localhost:3306
 Source Schema         : LAB_SYS

 Target Server Type    : MySQL
 Target Server Version : 80043 (8.0.43)
 File Encoding         : 65001

 Date: 09/04/2026 16:46:37
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for CLASSROOM_LIST
-- ----------------------------
DROP TABLE IF EXISTS `CLASSROOM_LIST`;
CREATE TABLE `CLASSROOM_LIST` (
  `id` int NOT NULL AUTO_INCREMENT,
  `NAME` varchar(255) NOT NULL COMMENT '教室名称',
  `LOCATION` varchar(255) DEFAULT '' COMMENT '教室位置',
  `CAPACITY` int DEFAULT 0 COMMENT '容纳人数',
  `STATUS` tinyint NOT NULL DEFAULT '0' COMMENT '状态码：0空闲 1已预约 2使用中 3维护中',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='实验室/教室列表';

-- ----------------------------
-- Records of CLASSROOM_LIST
-- ----------------------------
BEGIN;
INSERT INTO `CLASSROOM_LIST` (`id`, `NAME`, `LOCATION`, `CAPACITY`, `STATUS`) VALUES (1, '数字媒体实验室 A101', '综合楼 A 区 1 楼', 40, 0);
INSERT INTO `CLASSROOM_LIST` (`id`, `NAME`, `LOCATION`, `CAPACITY`, `STATUS`) VALUES (2, '人工智能实验室 A102', '综合楼 A 区 1 楼', 30, 0);
INSERT INTO `CLASSROOM_LIST` (`id`, `NAME`, `LOCATION`, `CAPACITY`, `STATUS`) VALUES (3, '影视后期实验室 B201', '综合楼 B 区 2 楼', 35, 0);
INSERT INTO `CLASSROOM_LIST` (`id`, `NAME`, `LOCATION`, `CAPACITY`, `STATUS`) VALUES (4, '网络工程实验室 B202', '综合楼 B 区 2 楼', 40, 0);
INSERT INTO `CLASSROOM_LIST` (`id`, `NAME`, `LOCATION`, `CAPACITY`, `STATUS`) VALUES (5, '虚拟现实实验室 C301', '综合楼 C 区 3 楼', 25, 0);
INSERT INTO `CLASSROOM_LIST` (`id`, `NAME`, `LOCATION`, `CAPACITY`, `STATUS`) VALUES (6, '软件工程实验室 C302', '综合楼 C 区 3 楼', 45, 0);
INSERT INTO `CLASSROOM_LIST` (`id`, `NAME`, `LOCATION`, `CAPACITY`, `STATUS`) VALUES (7, '语音实验室 D401', '综合楼 D 区 4 楼', 50, 0);
INSERT INTO `CLASSROOM_LIST` (`id`, `NAME`, `LOCATION`, `CAPACITY`, `STATUS`) VALUES (8, '计算机基础实验室 D402', '综合楼 D 区 4 楼', 60, 0);
INSERT INTO `CLASSROOM_LIST` (`id`, `NAME`, `LOCATION`, `CAPACITY`, `STATUS`) VALUES (9, '创新实践实验室 E501', '综合楼 E 区 5 楼', 20, 0);
COMMIT;

-- ----------------------------
-- Table structure for RENT_ANC
-- ----------------------------
DROP TABLE IF EXISTS `RENT_ANC`;
CREATE TABLE `RENT_ANC` (
  `ANNOUNCE` longtext COLLATE utf8mb4_unicode_ci
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Records of RENT_ANC
-- ----------------------------
BEGIN;
INSERT INTO `RENT_ANC` (`ANNOUNCE`) VALUES ('# 实验室预约系统使用须知\n\n欢迎使用实验室预约系统！请遵守以下规定：\n- 请提前至少 3 小时预约\n- 每次可选择多间教室\n- 使用完毕后请及时归还');
COMMIT;

-- ----------------------------
-- Table structure for RENT_LIST
-- ----------------------------
DROP TABLE IF EXISTS `RENT_LIST`;
CREATE TABLE `RENT_LIST` (
  `id` varchar(20) NOT NULL COMMENT '主键ID',
  `NAME` varchar(50) NOT NULL COMMENT '姓名',
  `NUM` varchar(15) NOT NULL COMMENT '学号',
  `TEL` varchar(15) NOT NULL COMMENT '手机号',
  `STATUS` tinyint NOT NULL COMMENT '特殊订单状态码，默认0已预约，1已完成，2逾期未还',
  `REMARK` varchar(255) DEFAULT '' COMMENT '备注信息',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='实验室预约记录表主表';

-- ----------------------------
-- Records of RENT_LIST
-- ----------------------------
BEGIN;
INSERT INTO `RENT_LIST` (`id`, `NAME`, `NUM`, `TEL`, `STATUS`, `REMARK`) VALUES ('202606080001', '张三', '2023013001', '13800138001', 1, '测试数据');
INSERT INTO `RENT_LIST` (`id`, `NAME`, `NUM`, `TEL`, `STATUS`, `REMARK`) VALUES ('202606080002', '李四', '2023013002', '13800138002', 0, '测试数据');
COMMIT;

-- ----------------------------
-- Table structure for RENT_ROOM (订单-教室关联表)
-- ----------------------------
DROP TABLE IF EXISTS `RENT_ROOM`;
CREATE TABLE `RENT_ROOM` (
  `id` int NOT NULL AUTO_INCREMENT,
  `rent_id` varchar(20) NOT NULL COMMENT '订单ID',
  `room_id` int NOT NULL COMMENT '教室ID',
  `BRWTIME` datetime NOT NULL COMMENT '预约时间',
  `RTNTIME` datetime NOT NULL COMMENT '归还时间',
  PRIMARY KEY (`id`),
  KEY `idx_rent_id` (`rent_id`),
  KEY `idx_room_id` (`room_id`),
  CONSTRAINT `fk_rent_room_rent` FOREIGN KEY (`rent_id`) REFERENCES `RENT_LIST` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_rent_room_room` FOREIGN KEY (`room_id`) REFERENCES `CLASSROOM_LIST` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='订单-教室关联表';

-- ----------------------------
-- Records of RENT_ROOM
-- ----------------------------
BEGIN;
INSERT INTO `RENT_ROOM` (`rent_id`, `room_id`, `BRWTIME`, `RTNTIME`) VALUES ('202606080001', 1, '2026-06-08 09:00:00', '2026-06-08 12:00:00');
INSERT INTO `RENT_ROOM` (`rent_id`, `room_id`, `BRWTIME`, `RTNTIME`) VALUES ('202606080002', 3, '2026-06-08 14:00:00', '2026-06-08 18:00:00');
COMMIT;

-- ----------------------------
-- Table structure for USER_SYS
-- ----------------------------
DROP TABLE IF EXISTS `USER_SYS`;
CREATE TABLE `USER_SYS` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '用户键ID',
  `USERNAME` varchar(255) NOT NULL COMMENT '用户名',
  `PASSWORD` varchar(255) NOT NULL COMMENT '密码',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户系统表';

-- ----------------------------
-- Records of USER_SYS
-- ----------------------------
BEGIN;
INSERT INTO `USER_SYS` (`id`, `USERNAME`, `PASSWORD`) VALUES (1, 'admin', 'UNAV@1LABLE');
INSERT INTO `USER_SYS` (`id`, `USERNAME`, `PASSWORD`) VALUES (2, 'lishuai', 'lishuai');
INSERT INTO `USER_SYS` (`id`, `USERNAME`, `PASSWORD`) VALUES (3, 'shijiu', 'shijiu');
COMMIT;

SET FOREIGN_KEY_CHECKS = 1;

-- ----------------------------
-- Table structure for TIME_SLOT
-- ----------------------------
DROP TABLE IF EXISTS `TIME_SLOT`;
CREATE TABLE `TIME_SLOT` (
  `id` int NOT NULL AUTO_INCREMENT,
  `day_of_week` tinyint NOT NULL COMMENT '星期几：1=周一，2=周二...7=周日',
  `period_index` tinyint NOT NULL DEFAULT 0 COMMENT '时间段索引：0=第一段，1=第二段',
  `time_range_start` time NOT NULL COMMENT '当日可预约开始时间（如 09:00:00）',
  `time_range_end` time NOT NULL COMMENT '当日可预约结束时间（如 18:00:00）',
  `enabled` tinyint NOT NULL DEFAULT 1 COMMENT '是否启用：0=禁用，1=启用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_day_period` (`day_of_week`, `period_index`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预约时间段配置表';

-- ----------------------------
-- Records of TIME_SLOT (周一~周五: 上午+下午, 周六~周日: 仅上午)
-- ----------------------------
BEGIN;
-- 周一
INSERT INTO `TIME_SLOT` (`day_of_week`, `period_index`, `time_range_start`, `time_range_end`, `enabled`) VALUES (1, 0, '09:00:00', '12:00:00', 1);
INSERT INTO `TIME_SLOT` (`day_of_week`, `period_index`, `time_range_start`, `time_range_end`, `enabled`) VALUES (1, 1, '14:00:00', '18:00:00', 1);
-- 周二
INSERT INTO `TIME_SLOT` (`day_of_week`, `period_index`, `time_range_start`, `time_range_end`, `enabled`) VALUES (2, 0, '09:00:00', '12:00:00', 1);
INSERT INTO `TIME_SLOT` (`day_of_week`, `period_index`, `time_range_start`, `time_range_end`, `enabled`) VALUES (2, 1, '14:00:00', '18:00:00', 1);
-- 周三
INSERT INTO `TIME_SLOT` (`day_of_week`, `period_index`, `time_range_start`, `time_range_end`, `enabled`) VALUES (3, 0, '09:00:00', '12:00:00', 1);
INSERT INTO `TIME_SLOT` (`day_of_week`, `period_index`, `time_range_start`, `time_range_end`, `enabled`) VALUES (3, 1, '14:00:00', '18:00:00', 1);
-- 周四
INSERT INTO `TIME_SLOT` (`day_of_week`, `period_index`, `time_range_start`, `time_range_end`, `enabled`) VALUES (4, 0, '09:00:00', '12:00:00', 1);
INSERT INTO `TIME_SLOT` (`day_of_week`, `period_index`, `time_range_start`, `time_range_end`, `enabled`) VALUES (4, 1, '14:00:00', '18:00:00', 1);
-- 周五
INSERT INTO `TIME_SLOT` (`day_of_week`, `period_index`, `time_range_start`, `time_range_end`, `enabled`) VALUES (5, 0, '09:00:00', '12:00:00', 1);
INSERT INTO `TIME_SLOT` (`day_of_week`, `period_index`, `time_range_start`, `time_range_end`, `enabled`) VALUES (5, 1, '14:00:00', '18:00:00', 1);
-- 周六
INSERT INTO `TIME_SLOT` (`day_of_week`, `period_index`, `time_range_start`, `time_range_end`, `enabled`) VALUES (6, 0, '09:00:00', '12:00:00', 1);
INSERT INTO `TIME_SLOT` (`day_of_week`, `period_index`, `time_range_start`, `time_range_end`, `enabled`) VALUES (6, 1, '09:00:00', '12:00:00', 0);
-- 周日
INSERT INTO `TIME_SLOT` (`day_of_week`, `period_index`, `time_range_start`, `time_range_end`, `enabled`) VALUES (7, 0, '09:00:00', '12:00:00', 1);
INSERT INTO `TIME_SLOT` (`day_of_week`, `period_index`, `time_range_start`, `time_range_end`, `enabled`) VALUES (7, 1, '09:00:00', '12:00:00', 0);
COMMIT;
