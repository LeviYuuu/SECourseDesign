package com.se.backend.controller;
import com.se.backend.common.Result;
import com.se.backend.service.DialogueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController @RequestMapping("/dialogue")
public class DialogueController {
    @Autowired private DialogueService dialogueService;

    @PostMapping("/next")
    public Result<Map<String, Object>> next(@RequestBody Map<String, Object> params) {
        // 修正点：安全转换 sessionId 为 Long
        Long sessionId = Long.valueOf(params.get("sessionId").toString());
        Integer currentRound = (Integer) params.get("currentRound");
        String userAnswer = (String) params.get("userAnswer");
        
        return Result.success(dialogueService.nextRound(sessionId, currentRound, userAnswer));
    }
}
