package com.se.backend.service.impl;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.se.backend.dto.ReportDetailDTO;
import com.se.backend.entity.EvaluationReport;
import com.se.backend.entity.SessionMessage;
import com.se.backend.entity.TrainingSession;
import com.se.backend.mapper.EvaluationReportMapper;
import com.se.backend.mapper.SessionMessageMapper;
import com.se.backend.mapper.TrainingSessionMapper;
import com.se.backend.service.EvaluationService;
import com.se.backend.service.LLMService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class EvaluationServiceImpl implements EvaluationService {  // 改为 implements 接口

    @Autowired private TrainingSessionMapper sessionMapper;
    @Autowired private SessionMessageMapper messageMapper;
    @Autowired private EvaluationReportMapper reportMapper;
    @Autowired private LLMService llmService;

    @Override
    public ReportDetailDTO getReportDetail(String sessionId) {
        Long sId = Long.valueOf(sessionId);

        // 1. 查询评估报告
        QueryWrapper<EvaluationReport> query = new QueryWrapper<>();
        query.eq("session_id", sId);
        EvaluationReport report = reportMapper.selectOne(query);

        if (report == null) {
            return null;
        }

        // 2. 转换为DTO
        ReportDetailDTO dto = new ReportDetailDTO();
        dto.setSessionId(sessionId);
        dto.setTotalScore(report.getTotalScore());
        dto.setGeneratedAt(report.getUpdatedAt() != null ? report.getUpdatedAt() : report.getCreatedAt());

        // 设置其他字段
        dto.setStrengths(new ArrayList<>());
        dto.setImprovements(new ArrayList<>());

        // 如果rawReportJson中有更多信息，可以解析
        if (report.getRawReportJson() != null && !report.getRawReportJson().isEmpty()) {
            try {
                JSONObject json = JSONUtil.parseObj(report.getRawReportJson());
                // 解析并设置更多字段...
            } catch (Exception e) {
                log.error("解析报告JSON失败", e);
            }
        }

        return dto;
    }

    @Override
    public void generateReport(String sessionId) {
        // 直接调用submitEvaluation来生成报告
        try {
            submitEvaluation(sessionId);
        } catch (Exception e) {
            log.error("生成报告失败", e);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Long submitEvaluation(String sessionId) {
        Long sId = Long.valueOf(sessionId);

        // 1. 校验会话状态
        TrainingSession session = sessionMapper.selectById(sId);
        if (session == null) {
            throw new RuntimeException("会话不存在");
        }

        // 2. 检查是否已存在报告
        QueryWrapper<EvaluationReport> query = new QueryWrapper<>();
        query.eq("session_id", sId);
        EvaluationReport existReport = reportMapper.selectOne(query);
        if (existReport != null) {
            return existReport.getReportId();
        }

        // 3. 创建初始报告记录
        EvaluationReport report = new EvaluationReport();
        report.setSessionId(sId);
        report.setStatus("GENERATING");
        report.setCreatedAt(LocalDateTime.now());
        reportMapper.insert(report);

        try {
            // 4. 获取完整对话历史
            QueryWrapper<SessionMessage> msgQuery = new QueryWrapper<>();
            msgQuery.eq("session_id", sId).orderByAsc("created_at");
            List<SessionMessage> history = messageMapper.selectList(msgQuery);

            if (history == null || history.isEmpty()) {
                throw new RuntimeException("对话记录为空，无法生成报告");
            }

            // 5. 构建 Prompt 并调用 LLM
            String prompt = buildEvaluationPrompt(history);
            String aiResponse = llmService.callAI(null, prompt);

            // 6. 解析结果并更新报告
            parseAndSaveReport(report, aiResponse);

            // 7. 更新 Session 状态为 COMPLETED
            session.setStatus("COMPLETED");
            sessionMapper.updateById(session);

        } catch (Exception e) {
            log.error("生成报告失败", e);
            report.setStatus("FAILED");
            report.setKeyIssues("生成失败: " + e.getMessage());
            reportMapper.updateById(report);
            throw new RuntimeException("生成报告失败: " + e.getMessage());
        }

        return report.getReportId();
    }

    @Override
    public EvaluationReport getReportBySessionId(String sessionId) {
        QueryWrapper<EvaluationReport> query = new QueryWrapper<>();
        query.eq("session_id", Long.valueOf(sessionId));
        return reportMapper.selectOne(query);
    }

    /**
     * 构建评估 Prompt
     */
    private String buildEvaluationPrompt(List<SessionMessage> history) {
        StringBuilder sb = new StringBuilder();
        sb.append("你是一位资深的面试官和职业教练。请根据以下的面试对话记录，对候选人的表现进行综合评估。\n\n");
        sb.append("【对话记录】：\n");

        for (SessionMessage msg : history) {
            String role = "USER".equalsIgnoreCase(msg.getSender()) ? "候选人" : "面试官";
            if (msg.getContentText() != null) {
                sb.append(role).append("：").append(msg.getContentText()).append("\n");
            }
        }

        sb.append("\n【评估要求】：\n");
        sb.append("1. 请客观、公正地分析候选人的逻辑思维、表达能力和专业素质。\n");
        sb.append("2. 请必须返回标准的 JSON 格式，不要包含 Markdown 代码块标记（如 ```json）。\n");
        sb.append("3. JSON 字段映射要求如下：\n");
        sb.append("   - total_score: 整数 (0-100)，总体评分。\n");
        sb.append("   - summary_strengths: 字符串，候选人的主要亮点和优势。\n");
        sb.append("   - key_issues: 字符串，候选人暴露出的主要问题或不足。\n");
        sb.append("   - next_actions: 字符串，给候选人的改进建议或下一步行动计划。\n");

        sb.append("\n【返回示例】：\n");
        sb.append("{\"total_score\": 85, \"summary_strengths\": \"逻辑清晰，回答有条理...\", \"key_issues\": \"语速过快，细节缺失...\", \"next_actions\": \"建议多练习STAR法则...\"}");

        return sb.toString();
    }

    /**
     * 解析 LLM 响应并保存
     */
    private void parseAndSaveReport(EvaluationReport report, String jsonStr) {
        try {
            // 清理可能的 Markdown 标记
            String cleanJson = jsonStr.replace("```json", "").replace("```", "").trim();

            // 尝试提取 JSON 部分
            if (!cleanJson.startsWith("{")) {
                int start = cleanJson.indexOf("{");
                int end = cleanJson.lastIndexOf("}");
                if (start >= 0 && end > start) {
                    cleanJson = cleanJson.substring(start, end + 1);
                }
            }

            JSONObject json = JSONUtil.parseObj(cleanJson);

            if (json.containsKey("total_score")) {
                report.setTotalScore(json.getInt("total_score"));
            }
            if (json.containsKey("summary_strengths")) {
                report.setSummaryStrengths(json.getStr("summary_strengths"));
            }
            if (json.containsKey("key_issues")) {
                report.setKeyIssues(json.getStr("key_issues"));
            }
            if (json.containsKey("next_actions")) {
                report.setNextActions(json.getStr("next_actions"));
            }

            report.setStatus("COMPLETED");
            report.setUpdatedAt(LocalDateTime.now());
            report.setRawReportJson(cleanJson);

            reportMapper.updateById(report);
        } catch (Exception e) {
            log.error("JSON解析失败, 原始响应: {}", jsonStr, e);
            throw new RuntimeException("AI响应格式错误，无法解析报告");
        }
    }
}