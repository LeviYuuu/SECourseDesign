package com.se.backend.controller;

import com.se.backend.common.Result;
import com.se.backend.entity.EvaluationReport;
import com.se.backend.service.EvaluationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/evaluation")
public class EvaluationController {

    @Autowired
    private EvaluationService evaluationService;

    /**
     * 提交生成报告任务
     * URL: POST /evaluation/submit
     */
    @PostMapping("/submit")
    public Result<Long> submitEvaluation(@RequestBody Map<String, String> params) {
        try {
            String sessionId = params.get("sessionId");
            if (sessionId == null) {
                return Result.error("sessionId 不能为空");
            }
            Long reportId = evaluationService.submitEvaluation(sessionId);
            return Result.success(reportId);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("提交评估失败: " + e.getMessage());
        }
    }

    /**
     * 获取报告详情
     * URL: GET /evaluation/report/{sessionId}
     */
    @GetMapping("/report/{sessionId}")
    public Result<EvaluationReport> getReport(@PathVariable String sessionId) {
        try {
            EvaluationReport report = evaluationService.getReportBySessionId(sessionId);
            if (report == null) {
                return Result.error("报告尚未生成或不存在");
            }
            return Result.success(report);
        } catch (Exception e) {
            return Result.error("获取报告失败: " + e.getMessage());
        }
    }
}