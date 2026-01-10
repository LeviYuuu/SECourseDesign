package com.se.backend.service;

import com.se.backend.entity.EvaluationReport;

public interface EvaluationService {
    /**
     * 提交评估任务
     * @param sessionId 会话ID
     * @return 生成的报告ID
     */
    Long submitEvaluation(String sessionId);

    /**
     * 获取评估报告
     * @param sessionId 会话ID
     * @return 报告详情
     */
    EvaluationReport getReportBySessionId(String sessionId);
}
