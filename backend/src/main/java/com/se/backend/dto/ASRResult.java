package com.se.backend.dto;

import lombok.Data;

@Data
public class ASRResult {
    private String transcript; // 转写文本
    private Double confidence; // 置信度
    private Long processingTime; // 处理耗时(ms)
}
