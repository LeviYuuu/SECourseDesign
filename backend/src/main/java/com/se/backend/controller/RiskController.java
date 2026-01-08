package com.se.backend.controller;
import com.se.backend.common.Result;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/risk")
public class RiskController {
    // 对应规约 4.7 内容安全
    @PostMapping("/check")
    public Result<Map<String, Object>> checkContent(@RequestBody Map<String, String> params) {
        String content = params.get("content");
        if(content != null && content.contains("作弊")) {
            return Result.success(Map.of("pass", false, "reason", "涉及敏感词"));
        }
        return Result.success(Map.of("pass", true));
    }
}
