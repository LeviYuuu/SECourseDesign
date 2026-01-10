package com.se.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("evaluation_reports")
public class EvaluationReport {
    @TableId(value = "report_id", type = IdType.AUTO)
    private Long reportId;

    @TableField("session_id")
    private Long sessionId;

    /**
     * 状态: GENERATING (生成中), COMPLETED (完成), FAILED (失败)
     */
    private String status;

    /**
     * 总评分 (0-100)
     */
    @TableField("total_score")
    private Integer totalScore;

    /**
     * 亮点总结
     */
    @TableField("summary_strengths")
    private String summaryStrengths;

    /**
     * 主要问题/不足
     */
    @TableField("key_issues")
    private String keyIssues;

    /**
     * 下一步建议/改进计划
     */
    @TableField("next_actions")
    private String nextActions;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
