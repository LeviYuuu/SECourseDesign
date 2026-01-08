package com.se.backend.controller;

import com.se.backend.common.Result;
import com.se.backend.service.DialogueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController @RequestMapping("/session")
public class SessionController {
    @Autowired private DialogueService dialogueService;

    @PostMapping("/create")
    public Result<Map<String, Object>> create(@RequestBody Map<String, Object> params) {
        // 1. 获取 userId
        Object userIdObj = params.get("userId");
        if (userIdObj == null) userIdObj = params.get("user_id");
        
        if (userIdObj == null) {
            return Result.error("创建失败：缺少 userId");
        }
        Long userId = Long.valueOf(userIdObj.toString());

        // 2. 获取场景 ID (严谨模式)
        Object scenarioObj = params.get("scenarioId");
        if (scenarioObj == null) scenarioObj = params.get("templateId"); // 兼容 templateId 字段
        if (scenarioObj == null) scenarioObj = params.get("template_id"); // 兼容下划线

        if (scenarioObj == null) {
            // ❌ 严谨逻辑：前端不传 ID，后端绝不猜，直接报错
            return Result.error("创建失败：请检查前端代码，必须传递 'scenarioId' 或 'templateId'");
        }
        
        Long templateId = Long.valueOf(scenarioObj.toString());

        // 3. 调用业务
        try {
            Long sessionId = dialogueService.createSession(userId, templateId);
            return Result.success(Map.of(
                "sessionId", sessionId.toString(), 
                "status", "created"
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("数据库错误：" + e.getMessage());
        }
    }
}
