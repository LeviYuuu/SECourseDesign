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
    private String endReason;

    @TableField("background_json")
    private String backgroundJson;

    @TableField("scenario_snapshot_json")
    private String scenarioSnapshotJson;

    @TableField("tts_enabled")
    private Boolean ttsEnabled;

    @TableField("current_round")
    private Integer currentRound;

    @TableField("started_at")
    private LocalDateTime startedAt;

    @TableField("ended_at")
    private LocalDateTime endedAt;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
