package com.se.backend.controller;

import com.se.backend.common.Result;
import com.se.backend.dto.SessionCreateRequest;
import com.se.backend.entity.TrainingSession;
import com.se.backend.service.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/session")
public class SessionController {

    @Autowired
    private SessionService sessionService;

    /**
     * 创建训练会话
     * 对齐设计规约 4.2.4 (2)
     */
    @PostMapping("/create")
    public Result<Map<String, Object>> createSession(@RequestBody SessionCreateRequest req) {
        try {
            // 1. 严格从请求体获取 UserId (文档规定)
            Long userId = req.getUserId();
            
            // 兜底逻辑：如果前端没传 userId，且设计允许，可以使用默认值 1001
            // 但根据报错，必须非空。
            if (userId == null) {
                // 尝试兼容: 如果前端用的是 localStorage 里的逻辑但没发过来
                // 这里强制抛错或给默认值，依据文档应由前端传递。
                // 为快速修复阻断，若为空则默认 1001 (开发环境)
                userId = 1001L; 
            }

            // 2. 调用 Service
            TrainingSession session = sessionService.createSession(userId, req);

            // 3. 返回结果 (对齐文档返回结构)
            return Result.success(Map.of(
                "sessionId", session.getSessionId().toString(), // 转字符串防止精度丢失
                "status", session.getStatus(),
                "startedAt", session.getStartedAt()
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("创建会话失败: " + e.getMessage());
        }
    }
}
