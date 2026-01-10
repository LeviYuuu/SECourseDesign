package com.se.backend.dto;

import lombok.Data;

@Data
public class ScenarioListItem {
    private Long templateId;
    private String title;
    private String description; // 对应文档要求的描述
    private String category;    // 对应 scene_type
    private String difficulty;
    private String rolePersona;
}
