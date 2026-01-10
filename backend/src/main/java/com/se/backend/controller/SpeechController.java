package com.se.backend.controller;

import com.se.backend.common.Result;
import com.se.backend.service.DialogueService;
import com.se.backend.service.SpeechService;
import com.se.backend.service.impl.DialogueServiceImpl; // 引入具体类以访问 getSessionStatus
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/speech")
public class SpeechController {

    @Autowired
    private SpeechService speechService;

    @Autowired
    private DialogueService dialogueService;

    // ... transcribe 保持不变 ...

    @PostMapping("/chat/response")
    public Result<Map<String, Object>> generateResponse(@RequestBody Map<String, Object> params) {
        try {
            String sessionId = (String) params.get("sessionId");
            // 第二个参数传 null，强迫 Service 使用数据库中的轮次
            Map<String, Object> dialogueResult = dialogueService.generateQuestion(sessionId, null, null);
            return Result.success(dialogueResult);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("AI 对话生成失败: " + e.getMessage());
        }
    }

    @PostMapping("/tts")
    public Result<Map<String, String>> generateTTS(@RequestBody Map<String, String> params) {
        try {
            String text = params.get("text");
            String voice = params.get("voice");
            String audioUrl = speechService.generateTTS(text, voice, null);
            Map<String, String> res = new HashMap<>();
            res.put("audioUrl", audioUrl);
            return Result.success(res);
        } catch (Exception e) {
            return Result.error("TTS失败: " + e.getMessage());
        }
    }

    // 【新增】获取会话状态接口
    @GetMapping("/session/status")
    public Result<Map<String, Object>> getSessionStatus(@RequestParam("sessionId") String sessionId) {
        try {
            // 强转为 Impl 以调用接口中未定义的扩展方法，或者您可以在 Interface 中定义它
            // 为保持简单，这里假设 DialogueService 接口已包含 getSessionStatus 
            // 如果 Interface 没改，请在 DialogueService.java 中添加 Map<String, Object> getSessionStatus(String sessionId);
            // 这里我们反射调用或者直接假定 Interface 已更新 (参考第1步修改)
            
            if (dialogueService instanceof DialogueServiceImpl) {
                 return Result.success(((DialogueServiceImpl) dialogueService).getSessionStatus(sessionId));
            }
            return Result.error("Service implementation error");
        } catch (Exception e) {
            return Result.error("获取状态失败: " + e.getMessage());
        }
    }
}
