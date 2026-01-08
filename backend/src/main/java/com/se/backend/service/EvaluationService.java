package com.se.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.se.backend.entity.EvaluationReport;
import com.se.backend.entity.TrainingSession;
import com.se.backend.mapper.EvaluationMapper;
import com.se.backend.mapper.SessionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class EvaluationService {

    @Autowired private EvaluationMapper evaluationMapper;
    @Autowired private SessionMapper sessionMapper;
    
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void generateReport(String sessionIdStr) {
        // 1. 将前端传来的 String ID 转为 Long
        Long sessionId = Long.valueOf(sessionIdStr);

        // 2. 更新会话结束时间
        TrainingSession s = sessionMapper.selectById(sessionId);
        if(s != null) {
            s.setStatus("COMPLETED");
            // 🚨 报错修复：数据库字段是 ended_at -> setEndedAt
            s.setEndedAt(LocalDateTime.now()); 
            sessionMapper.updateById(s);
        }

        // ... Mock 数据准备 (保持不变) ...
        Map<String, Object> mockData = new HashMap<>();
        List<Map<String, Object>> dims = new ArrayList<>();
        dims.add(Map.of("name", "逻辑性", "score", 8.5, "comment", "条理清晰"));
        mockData.put("dimensionScores", dims);
        // ... (省略其他 Mock 数据填充，为节省篇幅) ...
        
        // 3. 创建报告
        EvaluationReport r = new EvaluationReport();
        
        // 🚨 报错修复：这里必须传入 Long 类型的 sessionId
        r.setSessionId(sessionId); 
        
        r.setStatus("GENERATED");
        r.setTotalScore(85);
        r.setSummaryStrengths("声音洪亮;逻辑清晰");
        r.setKeyIssues("情感控制略显紧张");
        r.setCreatedAt(LocalDateTime.now());
        
        try {
            // 将 Mock 数据存入 JSON 字段
            r.setRawReportJson(objectMapper.writeValueAsString(mockData)); 
        } catch (Exception e) {
            r.setRawReportJson("{}");
        }

        evaluationMapper.insert(r);
    }

    public EvaluationReport getReport(String sessionIdStr) {
        // 查询时也需要转为 Long
        Long sessionId = Long.valueOf(sessionIdStr);
        QueryWrapper<EvaluationReport> qw = new QueryWrapper<>();
        qw.eq("session_id", sessionId);
        return evaluationMapper.selectOne(qw);
    }
}
