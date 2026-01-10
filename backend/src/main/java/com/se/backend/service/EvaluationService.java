package com.se.backend.service;

import com.se.backend.dto.ReportDetailDTO;
import com.se.backend.entity.EvaluationReport;
import org.springframework.transaction.annotation.Transactional;

public interface EvaluationService {

    ReportDetailDTO getReportDetail(String sessionId);

    void generateReport(String sessionId);

    @Transactional(rollbackFor = Exception.class)
    Long submitEvaluation(String sessionId);

    EvaluationReport getReportBySessionId(String sessionId);
}