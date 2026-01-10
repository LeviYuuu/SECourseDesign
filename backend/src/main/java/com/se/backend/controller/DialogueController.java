package com.se.backend.controller;

import com.se.backend.common.Result;
import com.se.backend.dto.MessageRequest;
import com.se.backend.dto.MessageResponse;
import com.se.backend.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/dialogue")
public class DialogueController {

    @Autowired
    private ChatService chatService;

    @PostMapping("/next")
    public Result<Map<String, Object>> next(@RequestBody Map<String, Object> params) {
        try {
            // 1. 解析参数
            Long sessionId = Long.valueOf(params.get("sessionId").toString());
            String content = params.get("userAnswer") != null ? params.get("userAnswer").toString() : "";
            
            // 模拟获取当前用户ID (实际应从Token获取)
            Long userId = 1001L;

            // 2. 构造请求 DTO
            MessageRequest req = new MessageRequest();
            req.setSessionId(sessionId);
            req.setInputMode("TEXT");
            req.setContent(content);

            // 3. 调用核心业务
            MessageResponse resp = chatService.processMessage(userId, req);

            // 【关键点】构造 Map
            Map<String, Object> data = new HashMap<>();
            data.put("round", resp.getRoundNo());
            
            // 核心：这里必须用 "question" 这个 Key，且 resp.getContent() 必须有值
            data.put("question", resp.getContent()); 
            data.put("content", resp.getContent()); // 双保险：同时返回 content
            
            data.put("hint", "请根据面试官的问题作答");
            data.put("isEnd", false);

            return Result.success(data);

        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("对话处理失败: " + e.getMessage());
        }
    }

    /**
     * 语音转写结果回调接口
     */
    @PostMapping("/transcript")
    public Result<Map<String, Object>> handleTranscript(@RequestBody Map<String, Object> params) {
        Long sessionId = Long.valueOf(params.get("sessionId").toString());
        String transcript = (String) params.get("transcript");
        Long userId = 1001L;

        MessageRequest req = new MessageRequest();
        req.setSessionId(sessionId);
        req.setInputMode("VOICE");
        req.setContent(transcript);
        req.setAsrText(transcript);

        MessageResponse resp = chatService.processMessage(userId, req);

        Map<String, Object> data = new HashMap<>();
        data.put("messageId", resp.getMessageId());
        data.put("status", "processed");
        data.put("aiReply", resp.getContent()); // 这里前端可能需要 aiReply

        return Result.success(data);
    }
    
    @GetMapping("/history")
    public Result<Object> getHistory(@RequestParam Long sessionId) {
        return Result.success(null); // 暂略
    }
}
