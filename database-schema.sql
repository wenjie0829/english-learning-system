-- 英语学习系统数据库设计
-- Database Schema for English Learning System

-- 用户表
CREATE TABLE `user` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `username` VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
  `password` VARCHAR(255) NOT NULL COMMENT '密码(加密)',
  `email` VARCHAR(100) UNIQUE COMMENT '邮箱',
  `role` ENUM('STUDENT', 'ADMIN') DEFAULT 'STUDENT' COMMENT '角色',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX idx_username (`username`),
  INDEX idx_email (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 单词表
CREATE TABLE `word` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `word` VARCHAR(100) NOT NULL COMMENT '单词',
  `phonetic` VARCHAR(100) COMMENT '音标',
  `definition` TEXT COMMENT '中文释义',
  `ai_definition` TEXT COMMENT 'AI生成的详细释义',
  `part_of_speech` VARCHAR(20) COMMENT '词性',
  `difficulty_level` ENUM('EASY', 'MEDIUM', 'HARD') DEFAULT 'MEDIUM' COMMENT '难度等级',
  `audio_url` VARCHAR(500) COMMENT '发音音频URL',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX idx_word (`word`),
  INDEX idx_difficulty (`difficulty_level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='单词表';

-- 例句表
CREATE TABLE `example_sentence` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `word_id` BIGINT NOT NULL COMMENT '单词ID',
  `sentence` TEXT NOT NULL COMMENT '英文例句',
  `translation` TEXT COMMENT '中文翻译',
  `audio_url` VARCHAR(500) COMMENT '例句发音音频URL',
  `is_original` BOOLEAN DEFAULT FALSE COMMENT '是否为原声例句',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  FOREIGN KEY (`word_id`) REFERENCES `word`(`id`) ON DELETE CASCADE,
  INDEX idx_word_id (`word_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='例句表';

-- 单词书表
CREATE TABLE `word_book` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `name` VARCHAR(100) NOT NULL COMMENT '单词书名称',
  `description` TEXT COMMENT '描述',
  `total_words` INT DEFAULT 0 COMMENT '总单词数',
  `category` VARCHAR(50) COMMENT '分类',
  `cover_url` VARCHAR(500) COMMENT '封面图片URL',
  `created_by` BIGINT COMMENT '创建者ID',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  FOREIGN KEY (`created_by`) REFERENCES `user`(`id`) ON DELETE SET NULL,
  INDEX idx_category (`category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='单词书表';

-- 单词书-单词关联表
CREATE TABLE `word_book_word` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `word_book_id` BIGINT NOT NULL COMMENT '单词书ID',
  `word_id` BIGINT NOT NULL COMMENT '单词ID',
  `order_index` INT DEFAULT 0 COMMENT '排序索引',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  FOREIGN KEY (`word_book_id`) REFERENCES `word_book`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`word_id`) REFERENCES `word`(`id`) ON DELETE CASCADE,
  UNIQUE KEY uk_book_word (`word_book_id`, `word_id`),
  INDEX idx_word_book_id (`word_book_id`),
  INDEX idx_order (`order_index`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='单词书-单词关联表';

-- 学习记录表
CREATE TABLE `learning_record` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `word_id` BIGINT NOT NULL COMMENT '单词ID',
  `word_book_id` BIGINT COMMENT '单词书ID',
  `status` ENUM('NEW', 'LEARNING', 'REVIEWING', 'MASTERED') DEFAULT 'NEW' COMMENT '学习状态',
  `review_count` INT DEFAULT 0 COMMENT '复习次数',
  `correct_count` INT DEFAULT 0 COMMENT '正确次数',
  `wrong_count` INT DEFAULT 0 COMMENT '错误次数',
  `last_review_at` TIMESTAMP NULL COMMENT '最后复习时间',
  `next_review_at` TIMESTAMP NULL COMMENT '下次复习时间(艾宾浩斯)',
  `ebbinghaus_stage` INT DEFAULT 0 COMMENT '艾宾浩斯阶段(0-7)',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`word_id`) REFERENCES `word`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`word_book_id`) REFERENCES `word_book`(`id`) ON DELETE SET NULL,
  INDEX idx_user_id (`user_id`),
  INDEX idx_word_id (`word_id`),
  INDEX idx_next_review (`next_review_at`),
  INDEX idx_status (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学习记录表';

-- 收藏表
CREATE TABLE `favorite` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `word_id` BIGINT NOT NULL COMMENT '单词ID',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`word_id`) REFERENCES `word`(`id`) ON DELETE CASCADE,
  UNIQUE KEY uk_user_word (`user_id`, `word_id`),
  INDEX idx_user_id (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收藏表';

-- 错词记录表
CREATE TABLE `wrong_word` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `word_id` BIGINT NOT NULL COMMENT '单词ID',
  `wrong_count` INT DEFAULT 1 COMMENT '错误次数',
  `last_wrong_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '最后错误时间',
  `resolved` BOOLEAN DEFAULT FALSE COMMENT '是否已掌握',
  `resolved_at` TIMESTAMP NULL COMMENT '掌握时间',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`word_id`) REFERENCES `word`(`id`) ON DELETE CASCADE,
  UNIQUE KEY uk_user_word (`user_id`, `word_id`),
  INDEX idx_user_id (`user_id`),
  INDEX idx_resolved (`resolved`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='错词记录表';

-- 学习统计表
CREATE TABLE `learning_statistics` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `stat_date` DATE NOT NULL COMMENT '统计日期',
  `words_learned` INT DEFAULT 0 COMMENT '学习新词数',
  `words_reviewed` INT DEFAULT 0 COMMENT '复习单词数',
  `words_correct` INT DEFAULT 0 COMMENT '正确数',
  `words_wrong` INT DEFAULT 0 COMMENT '错误数',
  `study_duration` INT DEFAULT 0 COMMENT '学习时长(分钟)',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
  UNIQUE KEY uk_user_date (`user_id`, `stat_date`),
  INDEX idx_stat_date (`stat_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学习统计表';

-- 口语学习内容表
CREATE TABLE `speaking_content` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `title` VARCHAR(200) NOT NULL COMMENT '标题',
  `category` VARCHAR(50) COMMENT '分类',
  `content` TEXT NOT NULL COMMENT '内容',
  `translation` TEXT COMMENT '中文翻译',
  `audio_url` VARCHAR(500) COMMENT '音频URL',
  `difficulty_level` ENUM('EASY', 'MEDIUM', 'HARD') DEFAULT 'MEDIUM' COMMENT '难度等级',
  `order_index` INT DEFAULT 0 COMMENT '排序',
  `created_by` BIGINT COMMENT '创建者ID',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  FOREIGN KEY (`created_by`) REFERENCES `user`(`id`) ON DELETE SET NULL,
  INDEX idx_category (`category`),
  INDEX idx_difficulty (`difficulty_level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='口语学习内容表';

-- 口语学习记录表
CREATE TABLE `speaking_record` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `content_id` BIGINT NOT NULL COMMENT '内容ID',
  `completed` BOOLEAN DEFAULT FALSE COMMENT '是否完成',
  `score` INT COMMENT '评分',
  `record_audio_url` VARCHAR(500) COMMENT '录音URL',
  `completed_at` TIMESTAMP NULL COMMENT '完成时间',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`content_id`) REFERENCES `speaking_content`(`id`) ON DELETE CASCADE,
  INDEX idx_user_id (`user_id`),
  INDEX idx_content_id (`content_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='口语学习记录表';

-- 初始化管理员用户（可选）
-- 建议通过应用注册用户后，在数据库中手动修改role字段为'ADMIN'
-- 或者使用正确的BCrypt哈希值插入管理员
-- INSERT INTO `user` (`username`, `password`, `email`, `role`) VALUES 
-- ('admin', '$2a$10$正确的BCrypt哈希值', 'admin@example.com', 'ADMIN');
