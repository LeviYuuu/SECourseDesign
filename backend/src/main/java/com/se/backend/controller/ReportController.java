package com.se.backend.controller;

import com.se.backend.common.Result;
import com.se.backend.service.EvaluationService;
import com.se.backend.dto.ReportDetailDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/report")
public class ReportController {

    @Autowired
    private EvaluationService evaluationService;

    @GetMapping("/detail")
    public Result<ReportDetailDTO> getReportDetail(@RequestParam String sessionId) {
        ReportDetailDTO report = evaluationService.getReportDetail(sessionId);
        if (report == null) {
            return Result.error("报告不存在或尚未生成");
        }
        return Result.success(report);
    }

    // 根据概要设计规约2.3.1，添加更多接口
    @GetMapping("/{sessionId}")
    public Result<ReportDetailDTO> getReport(@PathVariable String sessionId) {
        ReportDetailDTO report = evaluationService.getReportDetail(sessionId);
        if (report == null) {
            return Result.error("报告不存在");
        }
        return Result.success(report);
    }
}
