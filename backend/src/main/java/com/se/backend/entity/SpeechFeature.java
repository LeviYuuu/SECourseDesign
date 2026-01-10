package com.se.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("speech_features")
public class SpeechFeature {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("message_id")
    private Long messageId; // 关联 session_messages.message_id

    @TableField("speech_rate")
    private BigDecimal speechRate; // 字/分钟

    @TableField("pause_count")
    private Integer pauseCount;

    @TableField("pause_duration_ms")
    private Integer pauseDurationMs;

    @TableField("filler_word_rate")
    private BigDecimal fillerWordRate;

    @TableField("emotion_json")
    private String emotionJson;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
