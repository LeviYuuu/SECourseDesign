package com.se.backend.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class ReportDetailResponse {
    private Long reportId;
    private Long sessionId;
    private Integer totalScore;
    private String summaryStrengths;
    private String keyIssues;
    private String nextActions;
    private List<DimensionDto> dimensions;

    @Data
    public static class DimensionDto {
        private String dimension;
        private BigDecimal score;
        private String comment;
    }
}
