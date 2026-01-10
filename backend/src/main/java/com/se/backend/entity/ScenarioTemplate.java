package com.se.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("scenario_templates")
public class ScenarioTemplate {
    @TableId(value = "template_id", type = IdType.AUTO)
    private Long templateId;

    @TableField("scene_type")
    private String sceneType;

    private String title;

    @TableField("role_persona")
    private String rolePersona;

    private String difficulty;

    @TableField("default_rounds")
    private Integer defaultRounds;

    @TableField("config_json")
    private String configJson;

    private Integer version;

    @TableField("is_active")
    private Boolean isActive;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
