SET NAMES utf8mb4;
SET time_zone = '+00:00';

CREATE DATABASE IF NOT EXISTS `social_training`
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_0900_ai_ci;

USE `social_training`;

-- ----------------------------
-- 5.5 users
-- ----------------------------
CREATE TABLE IF NOT EXISTS `users` (
  `user_id` BIGINT NOT NULL AUTO_INCREMENT,
  `nickname` VARCHAR(64) NULL,
  `account_type` VARCHAR(16) NOT NULL,
  `account_no` VARCHAR(64) NOT NULL,
  `status` VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted_at` DATETIME NULL,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `uq_users_account` (`account_type`, `account_no`),
  KEY `idx_users_status` (`status`),
  KEY `idx_users_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- 5.6 user_credentials (1:1)
-- ----------------------------
CREATE TABLE IF NOT EXISTS `user_credentials` (
  `user_id` BIGINT NOT NULL,
  `password_hash` VARCHAR(128) NOT NULL,
  `password_salt` VARCHAR(64) NOT NULL,
  `last_login_at` DATETIME NULL,
  PRIMARY KEY (`user_id`),
  CONSTRAINT `fk_user_credentials_user`
    FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
    ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- 5.7 scenario_templates
-- ----------------------------
CREATE TABLE IF NOT EXISTS `scenario_templates` (
  `template_id` BIGINT NOT NULL AUTO_INCREMENT,
  `scene_type` VARCHAR(32) NOT NULL,
  `title` VARCHAR(128) NOT NULL,
  `role_persona` VARCHAR(64) NOT NULL,
  `difficulty` VARCHAR(8) NOT NULL,
  `default_rounds` INT NOT NULL,
  `config_json` JSON NULL,
  `version` INT NOT NULL DEFAULT 1,
  `is_active` BOOLEAN NOT NULL DEFAULT TRUE,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`template_id`),
  KEY `idx_scenario_scene_type` (`scene_type`),
  KEY `idx_scenario_active` (`scene_type`, `is_active`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- 5.8 training_sessions
-- ----------------------------
CREATE TABLE IF NOT EXISTS `training_sessions` (
  `session_id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `template_id` BIGINT NOT NULL,
  `status` VARCHAR(16) NOT NULL DEFAULT 'NOT_STARTED',
  `end_reason` VARCHAR(64) NULL,
  `background_json` JSON NULL,
  `scenario_snapshot_json` JSON NULL,
  `tts_enabled` BOOLEAN NOT NULL DEFAULT FALSE,
  `current_round` INT NOT NULL DEFAULT 0,
  `started_at` DATETIME NULL,
  `ended_at` DATETIME NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted_at` DATETIME NULL,
  PRIMARY KEY (`session_id`),
  KEY `idx_sessions_user_created` (`user_id`, `created_at` DESC),
  KEY `idx_sessions_template` (`template_id`),
  KEY `idx_sessions_status` (`status`),
  CONSTRAINT `fk_sessions_user`
    FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
    ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_sessions_template`
    FOREIGN KEY (`template_id`) REFERENCES `scenario_templates` (`template_id`)
    ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- 5.9 session_messages
-- (Doc suggests business-unique by (session_id, round_no, sender) for idempotency.)
-- ----------------------------
CREATE TABLE IF NOT EXISTS `session_messages` (
  `message_id` BIGINT NOT NULL AUTO_INCREMENT,
  `session_id` BIGINT NOT NULL,
  `round_no` INT NOT NULL,
  `sender` VARCHAR(8) NOT NULL,         -- SYSTEM / USER
  `stage` VARCHAR(32) NULL,
  `content_text` TEXT NULL,
  `input_mode` VARCHAR(8) NULL,         -- VOICE / TEXT; SYSTEM can be NULL
  `audio_uri` VARCHAR(255) NULL,
  `asr_text` TEXT NULL,
  `asr_confidence` DECIMAL(5,4) NULL,
  `corrected_text` TEXT NULL,
  `gen_source` VARCHAR(32) NULL,        -- LLM / FixedBank etc
  `llm_trace_id` VARCHAR(64) NULL,
  `relevance_hit` BOOLEAN NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`message_id`),
  UNIQUE KEY `uq_msg_session_round_sender` (`session_id`, `round_no`, `sender`),
  KEY `idx_msg_session_round_created` (`session_id`, `round_no`, `created_at`),
  KEY `idx_msg_session_created` (`session_id`, `created_at`),
  CONSTRAINT `fk_messages_session`
    FOREIGN KEY (`session_id`) REFERENCES `training_sessions` (`session_id`)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- 5.10 evaluation_reports (1 per session)
-- ----------------------------
CREATE TABLE IF NOT EXISTS `evaluation_reports` (
  `report_id` BIGINT NOT NULL AUTO_INCREMENT,
  `session_id` BIGINT NOT NULL,
  `status` VARCHAR(16) NOT NULL DEFAULT 'PENDING',  -- PENDING/READY/FAILED/BLOCKED
  `total_score` INT NULL,                            -- 0-100
  `summary_strengths` TEXT NULL,
  `key_issues` TEXT NULL,
  `next_actions` TEXT NULL,
  `raw_report_json` JSON NULL,
  `error_message` TEXT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`report_id`),
  UNIQUE KEY `uq_reports_session` (`session_id`),
  KEY `idx_reports_status` (`status`),
  CONSTRAINT `fk_reports_session`
    FOREIGN KEY (`session_id`) REFERENCES `training_sessions` (`session_id`)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- 5.11 report_dimension_scores
-- ----------------------------
CREATE TABLE IF NOT EXISTS `report_dimension_scores` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `report_id` BIGINT NOT NULL,
  `dimension_code` VARCHAR(32) NOT NULL,
  `score` DECIMAL(4,2) NOT NULL,
  `comment` TEXT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_dim_report_dimension` (`report_id`, `dimension_code`),
  KEY `idx_dim_report` (`report_id`),
  CONSTRAINT `fk_dim_report`
    FOREIGN KEY (`report_id`) REFERENCES `evaluation_reports` (`report_id`)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- 5.12 report_suggestions
-- ----------------------------
CREATE TABLE IF NOT EXISTS `report_suggestions` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `report_id` BIGINT NOT NULL,
  `category` VARCHAR(32) NULL,
  `suggestion_text` TEXT NOT NULL,
  `ref_message_id` BIGINT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_sug_report` (`report_id`),
  KEY `idx_sug_ref_msg` (`ref_message_id`),
  CONSTRAINT `fk_sug_report`
    FOREIGN KEY (`report_id`) REFERENCES `evaluation_reports` (`report_id`)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_sug_ref_message`
    FOREIGN KEY (`ref_message_id`) REFERENCES `session_messages` (`message_id`)
    ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- 5.13 report_rewrite_examples
-- ----------------------------
CREATE TABLE IF NOT EXISTS `report_rewrite_examples` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `report_id` BIGINT NOT NULL,
  `before_text` TEXT NOT NULL,
  `after_text` TEXT NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_rewrite_report` (`report_id`),
  CONSTRAINT `fk_rewrite_report`
    FOREIGN KEY (`report_id`) REFERENCES `evaluation_reports` (`report_id`)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- 5.14 risk_events
-- ----------------------------
CREATE TABLE IF NOT EXISTS `risk_events` (
  `risk_id` BIGINT NOT NULL AUTO_INCREMENT,
  `session_id` BIGINT NOT NULL,
  `message_id` BIGINT NULL,
  `risk_level` VARCHAR(16) NOT NULL,  -- LOW/MEDIUM/HIGH
  `risk_type` VARCHAR(32) NOT NULL,   -- HATE/SELF_HARM/PRIVACY...
  `action` VARCHAR(16) NOT NULL,      -- allow/warn/downgrade/terminate
  `detail` TEXT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`risk_id`),
  KEY `idx_risk_session_created` (`session_id`, `created_at` DESC),
  KEY `idx_risk_action` (`action`),
  KEY `idx_risk_message` (`message_id`),
  CONSTRAINT `fk_risk_session`
    FOREIGN KEY (`session_id`) REFERENCES `training_sessions` (`session_id`)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_risk_message`
    FOREIGN KEY (`message_id`) REFERENCES `session_messages` (`message_id`)
    ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- 5.15 speech_features (optional, 1:1 with message)
-- ----------------------------
CREATE TABLE IF NOT EXISTS `speech_features` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `message_id` BIGINT NOT NULL,
  `speech_rate` DECIMAL(6,2) NULL,
  `pause_count` INT NULL,
  `pause_duration_ms` INT NULL,
  `filler_word_rate` DECIMAL(6,4) NULL,
  `emotion_json` JSON NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_speech_message` (`message_id`),
  CONSTRAINT `fk_speech_message`
    FOREIGN KEY (`message_id`) REFERENCES `session_messages` (`message_id`)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- 5.16 user_metric_daily (optional)
-- ----------------------------
CREATE TABLE IF NOT EXISTS `user_metric_daily` (
  `user_id` BIGINT NOT NULL,
  `metric_date` DATE NOT NULL,
  `dimension_code` VARCHAR(32) NOT NULL,
  `avg_score` DECIMAL(4,2) NULL,
  `sample_count` INT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`, `metric_date`, `dimension_code`),
  KEY `idx_metric_user_date` (`user_id`, `metric_date` DESC),
  CONSTRAINT `fk_metric_user`
    FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- 5.17 evaluation_tasks
-- ----------------------------
CREATE TABLE IF NOT EXISTS `evaluation_tasks` (
  `task_id` BIGINT NOT NULL AUTO_INCREMENT,
  `session_id` BIGINT NOT NULL,
  `state` VARCHAR(16) NOT NULL,          -- QUEUED/RUNNING/SUCCEEDED/FAILED
  `retry_count` INT NOT NULL DEFAULT 0,
  `error_msg` TEXT NULL,
  `queued_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `started_at` DATETIME NULL,
  `finished_at` DATETIME NULL,
  PRIMARY KEY (`task_id`),
  KEY `idx_tasks_session` (`session_id`),
  KEY `idx_tasks_state_queued` (`state`, `queued_at`),
  CONSTRAINT `fk_tasks_session`
    FOREIGN KEY (`session_id`) REFERENCES `training_sessions` (`session_id`)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
