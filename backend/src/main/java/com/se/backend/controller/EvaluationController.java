package com.se.backend.controller;
import com.se.backend.common.Result;
import com.se.backend.entity.EvaluationReport;
import com.se.backend.service.EvaluationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController @RequestMapping("/evaluation")
public class EvaluationController {
    @Autowired private EvaluationService evaluationService;

    @PostMapping("/submit")
    public Result<String> submit(@RequestBody Map<String, String> params) {
        evaluationService.generateReport(params.get("sessionId"));
        return Result.success("生成中");
    }

    @GetMapping("/report")
    public Result<EvaluationReport> report(@RequestParam String sessionId) {
        return Result.success(evaluationService.getReport(sessionId));
    }
}
