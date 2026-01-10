package com.se.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("risk_events")
public class RiskEvent {
    @TableId(value = "risk_id", type = IdType.AUTO)
    private Long riskId;

    @TableField("session_id")
    private Long sessionId;

    @TableField("message_id")
    private Long messageId; // 触发该风控的消息ID

    @TableField("risk_level")
    private String riskLevel; // LOW, MEDIUM, HIGH

    @TableField("risk_type")
    private String riskType; // HATE, PRIVACY, etc.

    @TableField("action")
    private String action; // BLOCK, WARNING, NONE

    private String detail;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
