package com.se.backend.controller;
import com.se.backend.common.Result;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/speech")
public class SpeechController {
    // 对应规约 2.5.2 ASR接口 Mock
    @PostMapping("/transcribe")
    public Result<Map<String, Object>> transcribe() {
        return Result.success(Map.of(
            "transcript", "这是模拟的语音转文字结果",
            "confidence", 0.98
        ));
    }
}
