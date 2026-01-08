package com.se.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName(value = "evaluation_reports", autoResultMap = true)
public class EvaluationReport {
    @TableId(value = "report_id", type = IdType.AUTO)
    private Long reportId;

    @TableField("session_id")
    private Long sessionId;

    private String status;

    @TableField("total_score")
    private Integer totalScore;

    @TableField("summary_strengths")
    private String summaryStrengths;

    @TableField("key_issues")
    private String keyIssues;

    @TableField("next_actions")
    private String nextActions;

    // 重点：这是存储 Mock 复杂数据的 JSON 字段
    @TableField("raw_report_json")
    private String rawReportJson; 

    @TableField("error_message")
    private String errorMessage;

    @TableField("created_at")
    private LocalDateTime createdAt;
    
    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
