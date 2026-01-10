package com.se.backend.controller;

import com.se.backend.common.Result;
import com.se.backend.service.EvaluationService;
import com.se.backend.dto.ReportDetailDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/evaluation") // 前端 api/index.ts 中 submitEvaluation 指向 /evaluation/submit
public class EvaluationController {

    @Autowired
    private EvaluationService evaluationService;

    // 提交评估任务 (触发 LLM 分析)
    @PostMapping("/submit")
    public Result<Void> submitEvaluation(@RequestBody Map<String, String> payload) {
        String sessionId = payload.get("sessionId");
        evaluationService.generateReport(sessionId);
        return Result.success();
    }
}

// 补充 ReportController 对应前端 getReport 接口
@RestController
@RequestMapping("/report")
public class ReportController {

    @Autowired
    private EvaluationService evaluationService;

    @GetMapping("/detail")
    public Result<ReportDetailDTO> getReportDetail(@RequestParam String sessionId) {
        ReportDetailDTO report = evaluationService.getReportDetail(sessionId);
        return Result.success(report);
    }
}
