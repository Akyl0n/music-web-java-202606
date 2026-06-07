/*
 Navicat Premium Data Transfer

 Source Server         : localhost
 Source Server Type    : MySQL
 Source Server Version : 50739 (5.7.39-log)
 Source Host           : localhost:3306
 Source Schema         : yinyu

 Target Server Type    : MySQL
 Target Server Version : 50739 (5.7.39-log)
 File Encoding         : 65001

 Date: 25/04/2026 09:31:36
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for artist
-- ----------------------------
DROP TABLE IF EXISTS `artist`;
CREATE TABLE `artist`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '歌手名称',
  `gender` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '性别：male/female/group/unknown',
  `birthday` date NULL DEFAULT NULL COMMENT '出生日期',
  `region` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '地区/国籍',
  `type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'solo' COMMENT '歌手类型：solo/group/virtual',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '头像',
  `intro` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '歌手简介',
  `tags` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '标签，多个标签用英文逗号分隔',
  `letter` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '首字母/检索字母',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'enabled' COMMENT '状态：enabled/disabled',
  `sort_num` int(11) NOT NULL DEFAULT 1 COMMENT '排序值',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_artist_name`(`name`) USING BTREE,
  INDEX `idx_artist_letter`(`letter`) USING BTREE,
  INDEX `idx_artist_status`(`status`) USING BTREE,
  INDEX `idx_artist_sort_num`(`sort_num`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 256 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '歌手表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of artist
-- ----------------------------


-- ----------------------------
-- Table structure for home_recommend
-- ----------------------------
DROP TABLE IF EXISTS `home_recommend`;
CREATE TABLE `home_recommend`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `position_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '展示位编码：home_banner/home_daily_song',
  `target_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '关联类型：song/playlist/singer',
  `target_id` bigint(20) NOT NULL COMMENT '关联业务ID',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '自定义标题',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '描述文案',
  `cover` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '封面图，优先使用这里',
  `sort_num` int(11) NOT NULL DEFAULT 0 COMMENT '排序号，越大越靠前',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'enabled' COMMENT '状态：enabled/disabled',
  `create_time` datetime NULL DEFAULT NULL,
  `update_time` datetime NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_position_code`(`position_code`) USING BTREE,
  INDEX `idx_target`(`target_type`, `target_id`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE,
  INDEX `idx_sort_num`(`sort_num`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '首页推荐配置表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of home_recommend
-- ----------------------------


-- ----------------------------
-- Table structure for playlist
-- ----------------------------
DROP TABLE IF EXISTS `playlist`;
CREATE TABLE `playlist`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '歌单名称',
  `subtitle` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '副标题',
  `cover` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '歌单封面',
  `category` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '分类ID，对应字典 category',
  `tags` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '风格标签，多个用英文逗号分隔，对应字典 style',
  `intro` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '歌单简介',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'enabled' COMMENT '状态：enabled/disabled',
  `recommend_flag` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否推荐：0否1是',
  `song_count` int(11) NOT NULL DEFAULT 0 COMMENT '歌曲数量',
  `play_count` bigint(20) NOT NULL DEFAULT 0 COMMENT '播放次数',
  `favorite_count` bigint(20) NOT NULL DEFAULT 0 COMMENT '收藏次数',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_playlist_name`(`name`) USING BTREE,
  INDEX `idx_playlist_category`(`category`) USING BTREE,
  INDEX `idx_playlist_status`(`status`) USING BTREE,
  INDEX `idx_playlist_recommend_flag`(`recommend_flag`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '歌单表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of playlist
-- ----------------------------


-- ----------------------------
-- Table structure for playlist_song
-- ----------------------------
DROP TABLE IF EXISTS `playlist_song`;
CREATE TABLE `playlist_song`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `playlist_id` bigint(20) NOT NULL COMMENT '歌单ID',
  `song_id` bigint(20) NOT NULL COMMENT '歌曲ID',
  `sort_num` int(11) NOT NULL DEFAULT 1 COMMENT '排序值',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_playlist_song`(`playlist_id`, `song_id`) USING BTREE,
  INDEX `idx_playlist_song_playlist_id`(`playlist_id`) USING BTREE,
  INDEX `idx_playlist_song_song_id`(`song_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 119 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '歌单歌曲关联表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of playlist_song
-- ----------------------------


-- ----------------------------
-- Table structure for song
-- ----------------------------
DROP TABLE IF EXISTS `song`;
CREATE TABLE `song`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '歌曲名称',
  `subtitle` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '副标题',
  `singer_id` bigint(20) NOT NULL COMMENT '歌手ID',
  `singer_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '歌手名称，冗余存储',
  `category` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '分类，对应字典 category',
  `tags` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '风格标签，多个用英文逗号分隔，对应字典 style',
  `cover` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '歌曲封面',
  `audio_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '音频文件地址',
  `duration_seconds` int(11) NULL DEFAULT NULL COMMENT '时长，单位秒',
  `language` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '语言',
  `intro` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '歌曲简介',
  `release_date` date NULL DEFAULT NULL COMMENT '发行日期',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'enabled' COMMENT '状态：enabled/disabled',
  `recommend_flag` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否推荐：0否1是',
  `play_count` bigint(20) NOT NULL DEFAULT 0 COMMENT '播放次数',
  `like_count` bigint(20) NOT NULL DEFAULT 0 COMMENT '点赞次数',
  `favorite_count` bigint(20) NOT NULL DEFAULT 0 COMMENT '收藏次数',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_song_name`(`name`) USING BTREE,
  INDEX `idx_song_singer_id`(`singer_id`) USING BTREE,
  INDEX `idx_song_category`(`category`) USING BTREE,
  INDEX `idx_song_status`(`status`) USING BTREE,
  INDEX `idx_song_recommend_flag`(`recommend_flag`) USING BTREE,
  INDEX `idx_song_release_date`(`release_date`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 52 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '歌曲表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of song
-- ----------------------------


-- ----------------------------
-- Table structure for sys_dict
-- ----------------------------
DROP TABLE IF EXISTS `sys_dict`;
CREATE TABLE `sys_dict`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `parent_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '父级ID，0表示一级类型',
  `dict_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '字典编码',
  `dict_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '字典名称',
  `sort_num` int(11) NOT NULL DEFAULT 0 COMMENT '排序号',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'enabled' COMMENT '状态：enabled/disabled',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_parent_code`(`parent_id`, `dict_code`) USING BTREE,
  INDEX `idx_parent_id`(`parent_id`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE,
  INDEX `idx_sort_num`(`sort_num`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 34 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '系统字典表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_dict
-- ----------------------------
INSERT INTO `sys_dict` VALUES (1, 0, 'country', '国家', 10, 'enabled', '统一维护国家地区数据');
INSERT INTO `sys_dict` VALUES (2, 0, 'style', '风格', 20, 'enabled', '统一维护歌曲与歌手风格');
INSERT INTO `sys_dict` VALUES (3, 0, 'category', '分类', 30, 'enabled', '统一维护场景分类');
INSERT INTO `sys_dict` VALUES (4, 1, 'CN', '内地', 10, 'enabled', '默认站点国家');
INSERT INTO `sys_dict` VALUES (5, 1, 'JP', '日本', 40, 'enabled', '后续扩展东方音乐');
INSERT INTO `sys_dict` VALUES (6, 2, 'gufeng', '古风', 10, 'enabled', '站内主风格');
INSERT INTO `sys_dict` VALUES (7, 2, 'xiqiang', '戏腔', 20, 'enabled', '特色风格标签');
INSERT INTO `sys_dict` VALUES (8, 2, 'instrumental', '纯音乐', 30, 'enabled', '器乐向');
INSERT INTO `sys_dict` VALUES (9, 3, 'study', '学习专注', 10, 'enabled', '场景分类');
INSERT INTO `sys_dict` VALUES (10, 3, 'night', '深夜循环', 20, 'enabled', '夜听场景');
INSERT INTO `sys_dict` VALUES (11, 3, 'travel', '旅行通勤', 30, 'enabled', '出行场景');
INSERT INTO `sys_dict` VALUES (12, 1, 'GT', '港台', 20, 'enabled', NULL);
INSERT INTO `sys_dict` VALUES (13, 1, 'US', '欧美', 30, 'enabled', NULL);
INSERT INTO `sys_dict` VALUES (14, 1, 'KR', '韩国', 41, 'enabled', NULL);
INSERT INTO `sys_dict` VALUES (15, 2, 'pop', '流行', 40, 'enabled', '大众流行风格');
INSERT INTO `sys_dict` VALUES (16, 2, 'rock', '摇滚', 50, 'enabled', '摇滚风格');
INSERT INTO `sys_dict` VALUES (17, 2, 'electronic', '电子', 60, 'enabled', '电子音乐风格');
INSERT INTO `sys_dict` VALUES (18, 2, 'folk', '民谣', 70, 'enabled', '民谣风格');
INSERT INTO `sys_dict` VALUES (19, 2, 'jazz', '爵士', 80, 'enabled', '爵士风格');
INSERT INTO `sys_dict` VALUES (20, 2, 'classical', '古典', 90, 'enabled', '古典音乐风格');
INSERT INTO `sys_dict` VALUES (21, 2, 'r_and_b', 'R&B', 100, 'enabled', '节奏蓝调风格');
INSERT INTO `sys_dict` VALUES (22, 2, 'rap', '说唱', 110, 'enabled', '说唱风格');
INSERT INTO `sys_dict` VALUES (23, 2, 'country_music', '乡村', 120, 'enabled', '乡村音乐风格');
INSERT INTO `sys_dict` VALUES (24, 2, 'latin', '拉丁', 130, 'enabled', '拉丁音乐风格');
INSERT INTO `sys_dict` VALUES (25, 3, 'sports', '运动健身', 40, 'enabled', '运动健身场景');
INSERT INTO `sys_dict` VALUES (26, 3, 'meditation', '冥想放松', 50, 'enabled', '冥想、放松场景');
INSERT INTO `sys_dict` VALUES (27, 3, 'party', '派对聚会', 60, 'enabled', '派对、聚会场景');
INSERT INTO `sys_dict` VALUES (28, 3, 'work', '工作背景', 70, 'enabled', '工作时聆听的背景音乐');
INSERT INTO `sys_dict` VALUES (29, 3, 'cafe', '咖啡厅', 80, 'enabled', '咖啡厅、休闲场景');
INSERT INTO `sys_dict` VALUES (30, 3, 'reading', '阅读', 90, 'enabled', '阅读时配乐');
INSERT INTO `sys_dict` VALUES (31, 3, 'driving', '驾驶', 100, 'enabled', '驾车出行场景');
INSERT INTO `sys_dict` VALUES (32, 3, 'sleep', '助眠', 110, 'enabled', '辅助睡眠场景');
INSERT INTO `sys_dict` VALUES (33, 3, 'yueyu', '经典粤语', 111, 'enabled', NULL);

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码，建议存加密后的密文',
  `nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '昵称',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '头像',
  `gender` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '性别：male/female/unknown',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '邮箱',
  `signature` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '个性签名',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'enabled' COMMENT '状态：enabled/disabled',
  `last_login_time` datetime NULL DEFAULT NULL COMMENT '最后登录时间',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_email`(`email`) USING BTREE,
  INDEX `idx_user_nickname`(`nickname`) USING BTREE,
  INDEX `idx_user_status`(`status`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of user
-- ----------------------------


-- ----------------------------
-- Table structure for user_action
-- ----------------------------
DROP TABLE IF EXISTS `user_action`;
CREATE TABLE `user_action`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `action_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '行为类型：like_song/favorite_playlist/play_song',
  `target_id` bigint(20) NOT NULL COMMENT '目标ID，歌曲ID或歌单ID',
  `action_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '行为时间',
  `progress_seconds` int(11) NULL DEFAULT 0 COMMENT '播放进度，单位秒，仅 play_song 使用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_action`(`user_id`, `action_type`, `target_id`) USING BTREE,
  INDEX `idx_user_action_user_id`(`user_id`) USING BTREE,
  INDEX `idx_user_action_type`(`action_type`) USING BTREE,
  INDEX `idx_user_action_target_id`(`target_id`) USING BTREE,
  INDEX `idx_user_action_time`(`action_time`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 16 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户行为表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of user_action
-- ----------------------------


SET FOREIGN_KEY_CHECKS = 1;
