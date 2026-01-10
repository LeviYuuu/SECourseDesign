package com.se.backend.dto;

import lombok.Data;

/**
 * 严格对齐设计规约 4.2.4 (2) 参数示例
 * {
 * "userId": 1001,
 * "scenarioId": 1,
 * "config": { ... }
 * }
 */
@Data
public class SessionCreateRequest {
    private Long userId;        // 必须包含 userId
    private Long templateId;    // 对应 scenarioId (文档称 scenarioId，代码映射为 templateId)
    private Long scenarioId;    // 兼容字段：为了匹配文档中的 JSON key "scenarioId"
    
    // config 结构
    private Boolean ttsEnabled;
    private String difficulty;
    private String rolePersona;
    private Integer rounds;

    // 兼容逻辑：获取 TemplateId
    public Long getTemplateId() {
        if (templateId != null) return templateId;
        return scenarioId;
    }
}
