package com.se.backend.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
public class ReportDetailDTO {
    private String sessionId;
    private Integer totalScore; // 总分
    private List<DimensionScoreDTO> dimensionScores; // 维度评分列表
    private List<String> strengths; // 优势
    private List<String> improvements; // 改进建议（或者使用suggestions）
    private List<SuggestionDTO> suggestions; // 具体建议
    private List<RewriteExampleDTO> rewriteExamples; // 改写示例
    private LocalDateTime generatedAt; // 报告生成时间
}

// 维度评分DTO
@Data
class DimensionScoreDTO {
    private String name;
    private Integer score;
    private String comment;
}

// 建议DTO
@Data
class SuggestionDTO {
    private String action;
    private String why;
    private String how;
}

// 改写示例DTO
@Data
class RewriteExampleDTO {
    private String before;
    private String after;
}
