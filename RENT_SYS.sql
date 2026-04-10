/*
 Navicat Premium Data Transfer

 Source Server         : MacLocal
 Source Server Type    : MySQL
 Source Server Version : 80043 (8.0.43)
 Source Host           : localhost:3306
 Source Schema         : RENT_SYS

 Target Server Type    : MySQL
 Target Server Version : 80043 (8.0.43)
 File Encoding         : 65001

 Date: 09/04/2026 16:46:37
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for DEVICE_LIST
-- ----------------------------
DROP TABLE IF EXISTS `DEVICE_LIST`;
CREATE TABLE `DEVICE_LIST` (
  `id` int NOT NULL AUTO_INCREMENT,
  `NAME` varchar(255) NOT NULL COMMENT '器材名称',
  `BRAND` varchar(255) NOT NULL COMMENT '器材品牌',
  `TYPE` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '器材类型',
  `STATUS` tinyint NOT NULL DEFAULT '0' COMMENT '状态码：0正常库存 1已预约借出 2逾期未还 3设备故障',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='摄影器材列表';

-- ----------------------------
-- Records of DEVICE_LIST
-- ----------------------------
BEGIN;
INSERT INTO `DEVICE_LIST` (`id`, `NAME`, `BRAND`, `TYPE`, `STATUS`) VALUES (1, 'SONY A7M4-01', '索尼/SONY', '相机/Camara', 0);
INSERT INTO `DEVICE_LIST` (`id`, `NAME`, `BRAND`, `TYPE`, `STATUS`) VALUES (2, 'SONY A7M4-02', '索尼/SONY', '相机/Camara', 2);
INSERT INTO `DEVICE_LIST` (`id`, `NAME`, `BRAND`, `TYPE`, `STATUS`) VALUES (3, 'SONY A7M4-03', '索尼/SONY', '相机/Camara', 2);
INSERT INTO `DEVICE_LIST` (`id`, `NAME`, `BRAND`, `TYPE`, `STATUS`) VALUES (4, 'SONY A7M4-04', '索尼/SONY', '相机/Camara', 1);
INSERT INTO `DEVICE_LIST` (`id`, `NAME`, `BRAND`, `TYPE`, `STATUS`) VALUES (5, 'SONY Cinema FX3', '索尼/SONY', '相机/Camara', 1);
INSERT INTO `DEVICE_LIST` (`id`, `NAME`, `BRAND`, `TYPE`, `STATUS`) VALUES (6, 'FE 24-70mm OSS', '索尼/SONY', '镜头/Lens', 0);
INSERT INTO `DEVICE_LIST` (`id`, `NAME`, `BRAND`, `TYPE`, `STATUS`) VALUES (7, 'FE 24-105mm OSS', '索尼/SONY', '镜头/Lens', 0);
INSERT INTO `DEVICE_LIST` (`id`, `NAME`, `BRAND`, `TYPE`, `STATUS`) VALUES (8, 'SONY Cinema FX3', '索尼/SONY', '相机/Camara', 1);
INSERT INTO `DEVICE_LIST` (`id`, `NAME`, `BRAND`, `TYPE`, `STATUS`) VALUES (9, 'SONY Cinema FX3', '索尼/SONY', '相机/Camara', 0);
INSERT INTO `DEVICE_LIST` (`id`, `NAME`, `BRAND`, `TYPE`, `STATUS`) VALUES (10, 'SONY Cinema FX3', '索尼/SONY', '相机/Camara', 0);
INSERT INTO `DEVICE_LIST` (`id`, `NAME`, `BRAND`, `TYPE`, `STATUS`) VALUES (11, 'MEKE 35mm f1.8', 'MEKE', '镜头', 1);
INSERT INTO `DEVICE_LIST` (`id`, `NAME`, `BRAND`, `TYPE`, `STATUS`) VALUES (12, 'MEKE', 'MEKE', 'lens', 1);
INSERT INTO `DEVICE_LIST` (`id`, `NAME`, `BRAND`, `TYPE`, `STATUS`) VALUES (13, 'DJI Nano 3', '大疆DJI', '云台', 1);
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
INSERT INTO `RENT_ANC` (`ANNOUNCE`) VALUES ('# AHHHHHHHH！');
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
  `CAMARA` int DEFAULT NULL COMMENT '相机型号',
  `LENS` int DEFAULT NULL COMMENT '镜头型号',
  `OTHER` int DEFAULT NULL COMMENT '其他设备型号',
  `BRWTIME` datetime NOT NULL COMMENT '预约时间',
  `RTNTIME` datetime NOT NULL COMMENT '归还时间',
  `STATUS` tinyint NOT NULL COMMENT '特殊订单状态码，默认0借出中，1已归还，2逾期未还',
  `REMARK` varchar(255) DEFAULT '' COMMENT '备注信息',
  PRIMARY KEY (`id`),
  KEY `Camara` (`CAMARA`),
  KEY `Lens` (`LENS`),
  KEY `Stabilizer` (`OTHER`),
  CONSTRAINT `Camara` FOREIGN KEY (`CAMARA`) REFERENCES `DEVICE_LIST` (`id`) ON DELETE RESTRICT,
  CONSTRAINT `Lens` FOREIGN KEY (`LENS`) REFERENCES `DEVICE_LIST` (`id`) ON DELETE RESTRICT,
  CONSTRAINT `Stabilizer` FOREIGN KEY (`OTHER`) REFERENCES `DEVICE_LIST` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='摄影器材预约记录表主表';

-- ----------------------------
-- Records of RENT_LIST
-- ----------------------------
BEGIN;
INSERT INTO `RENT_LIST` (`id`, `NAME`, `NUM`, `TEL`, `CAMARA`, `LENS`, `OTHER`, `BRWTIME`, `RTNTIME`, `STATUS`, `REMARK`) VALUES ('202604030001', '蔚嘉琪', '2023013090', '15036156580', 1, NULL, NULL, '2026-04-03 14:23:54', '2026-04-04 14:23:57', 2, '测试数据');
INSERT INTO `RENT_LIST` (`id`, `NAME`, `NUM`, `TEL`, `CAMARA`, `LENS`, `OTHER`, `BRWTIME`, `RTNTIME`, `STATUS`, `REMARK`) VALUES ('202604030002', '王乾博', '2020023020', '13632418372', 2, NULL, NULL, '2026-04-03 16:17:52', '2026-04-04 16:17:56', 1, '测试数据');
INSERT INTO `RENT_LIST` (`id`, `NAME`, `NUM`, `TEL`, `CAMARA`, `LENS`, `OTHER`, `BRWTIME`, `RTNTIME`, `STATUS`, `REMARK`) VALUES ('202604050001', 'jij', '2039482910', '15349203918', 2, NULL, NULL, '2026-04-05 14:00:00', '2026-04-08 14:00:00', 2, '数据测试');
INSERT INTO `RENT_LIST` (`id`, `NAME`, `NUM`, `TEL`, `CAMARA`, `LENS`, `OTHER`, `BRWTIME`, `RTNTIME`, `STATUS`, `REMARK`) VALUES ('202604050002', '陈玲聿', '2023013029', '13829489348', 1, NULL, NULL, '2026-04-05 00:00:00', '2026-04-06 00:00:00', 2, '数据测试');
INSERT INTO `RENT_LIST` (`id`, `NAME`, `NUM`, `TEL`, `CAMARA`, `LENS`, `OTHER`, `BRWTIME`, `RTNTIME`, `STATUS`, `REMARK`) VALUES ('202604050003', '蔚嘉琪', '2023013090', '15036156580', 5, 6, NULL, '2026-04-05 10:00:00', '2026-04-06 17:00:00', 1, '测试数据');
INSERT INTO `RENT_LIST` (`id`, `NAME`, `NUM`, `TEL`, `CAMARA`, `LENS`, `OTHER`, `BRWTIME`, `RTNTIME`, `STATUS`, `REMARK`) VALUES ('202604050004', '为佳', '29304903', '13647382948', 3, NULL, NULL, '2026-04-05 11:17:00', '2026-04-05 14:00:00', 2, '测试数据');
INSERT INTO `RENT_LIST` (`id`, `NAME`, `NUM`, `TEL`, `CAMARA`, `LENS`, `OTHER`, `BRWTIME`, `RTNTIME`, `STATUS`, `REMARK`) VALUES ('202604050005', '应雨希', '2023013787', '15876492887', 3, NULL, NULL, '2026-04-06 11:00:00', '2026-04-07 08:00:00', 2, '手机测试');
INSERT INTO `RENT_LIST` (`id`, `NAME`, `NUM`, `TEL`, `CAMARA`, `LENS`, `OTHER`, `BRWTIME`, `RTNTIME`, `STATUS`, `REMARK`) VALUES ('202604090001', '英语系', '2023013783', '19191991919', 8, 11, NULL, '2026-04-09 19:18:00', '2026-04-10 00:00:00', 0, 'yyx是大sharkbee');
INSERT INTO `RENT_LIST` (`id`, `NAME`, `NUM`, `TEL`, `CAMARA`, `LENS`, `OTHER`, `BRWTIME`, `RTNTIME`, `STATUS`, `REMARK`) VALUES ('202604090002', 'Jiaqi Kirby Yu', '2023013090', '15036156580', 5, 12, 13, '2026-04-09 20:04:00', '2026-04-11 00:00:00', 0, 'yiyayiyayo');
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
