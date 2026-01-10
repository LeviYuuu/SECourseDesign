package com.se.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("evaluation_reports")
public class EvaluationReport {
    @TableId(type = IdType.AUTO)
    private Long reportId;
    private Long sessionId;
    private String status;
    private Integer totalScore;
    private String summaryStrengths;
    private String keyIssues;
    private String nextActions;
    private String rawReportJson;  // 存储完整的JSON报告
    private String errorMessage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}