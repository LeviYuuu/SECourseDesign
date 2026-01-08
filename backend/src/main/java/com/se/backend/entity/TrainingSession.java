package com.se.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("training_sessions")
public class TrainingSession {
    @TableId(value = "session_id", type = IdType.AUTO)
    private Long sessionId;

    @TableField("user_id")
    private Long userId;

    @TableField("template_id")
    private Long templateId;

    private String status;

    @TableField("end_reason")
    private String endReason; // 补充字段

    @TableField("current_round")
    private Integer currentRound;

    @TableField("tts_enabled")
    private Boolean ttsEnabled; // 补充字段

    @TableField("started_at")
    private LocalDateTime startedAt;

    @TableField("ended_at")
    private LocalDateTime endedAt;

    @TableField("created_at")
    private LocalDateTime createdAt;
    
    @TableField("updated_at")
    private LocalDateTime updatedAt;

    @TableField("deleted_at")
    private LocalDateTime deletedAt;
}
