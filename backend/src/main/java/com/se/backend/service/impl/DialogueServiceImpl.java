package com.se.backend.service.impl;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.se.backend.entity.ScenarioTemplate;
import com.se.backend.entity.SessionMessage;
import com.se.backend.entity.TrainingSession;
import com.se.backend.mapper.ScenarioTemplateMapper;
import com.se.backend.mapper.SessionMessageMapper;
import com.se.backend.mapper.TrainingSessionMapper;
import com.se.backend.service.DialogueService;
import com.se.backend.service.LLMService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DialogueServiceImpl implements DialogueService {

    @Autowired private TrainingSessionMapper sessionMapper;
    @Autowired private SessionMessageMapper messageMapper;
    @Autowired private ScenarioTemplateMapper templateMapper;
    @Autowired private LLMService llmService;

    @Override
    public Map<String, Object> generateQuestion(String sessionId, Integer frontendRoundIgnore, List<SessionMessage> history) {
        Long sId = Long.valueOf(sessionId);
        TrainingSession session = sessionMapper.selectById(sId);
        if (session == null) throw new RuntimeException("Session not found");

        // 【关键修复1】强制使用数据库中的真实轮次，忽略前端传入的可能错误的轮次
        Integer realRound = session.getCurrentRound();
        if (realRound == null) realRound = 1;

        // 1. 获取场景配置
        String rolePersona = "专业的面试官";
        Integer maxRounds = 10; 

        if (session.getTemplateId() != null) {
            ScenarioTemplate tpl = templateMapper.selectById(session.getTemplateId());
            if (tpl != null) {
                if (tpl.getRolePersona() != null && !tpl.getRolePersona().isEmpty()) {
                    rolePersona = tpl.getRolePersona();
                }
                if (tpl.getDefaultRounds() != null) {
                    maxRounds = tpl.getDefaultRounds();
                }
            }
        }

        // 2. 判定结束条件 (当前轮次 >= 最大轮次)
        boolean isLastRound = (realRound >= maxRounds);

        // 3. 准备 Prompt
        List<SessionMessage> effectiveHistory = history;
        if (effectiveHistory == null || effectiveHistory.isEmpty()) {
            effectiveHistory = this.getDialogueHistory(sessionId);
        }

        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append("你现在的身份是：").append(rolePersona).append("。\n");
        promptBuilder.append("场景任务：你正在对候选人进行面试。\n");
        
        if (isLastRound) {
            promptBuilder.append("【关键指令】：这是面试的最后一个问题（第 "+realRound+"/"+maxRounds+" 轮）。请根据候选人的回答及整体表现做一个简短的总结评价，并礼貌地结束面试，不要再提出新的问题。\n");
        } else {
            promptBuilder.append("当前进度：第 ").append(realRound).append(" 轮，共 ").append(maxRounds).append(" 轮。\n");
            promptBuilder.append("【关键指令】：请继续针对候选人的回答提出有深度的追问。\n");
        }

        promptBuilder.append("约束条件：请以JSON格式返回，格式为：{\"content\": \"你的内容\"}");

        // 4. 调用 LLM
        String aiResponse = llmService.callAI(effectiveHistory, promptBuilder.toString());
        String content = extractContent(aiResponse);

        // 5. 存库
        SessionMessage sysMsg = new SessionMessage();
        sysMsg.setSessionId(sId);
        sysMsg.setRoundNo(realRound);
        sysMsg.setSender("SYSTEM");
        sysMsg.setContentText(content);
        sysMsg.setGenSource("LLM");
        sysMsg.setCreatedAt(LocalDateTime.now());
        messageMapper.insert(sysMsg);
        
        // 6. 更新 Session
        if (isLastRound) {
            session.setStatus("COMPLETED");
            // 最后一轮不再自增，保持在最大轮次
        } else {
            session.setCurrentRound(realRound + 1);
        }
        session.setUpdatedAt(LocalDateTime.now());
        sessionMapper.updateById(session);

        // 7. 返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("question", content);
        result.put("content", content);
        // 返回更新后的下一次轮次（或者当前轮次 if end）
        result.put("round", session.getCurrentRound());
        result.put("totalRounds", maxRounds);
        result.put("isEnd", isLastRound);
        result.put("hint", "请回答面试官的问题");
        
        return result;
    }

    // 【新增】获取会话状态，用于前端初始化
    public Map<String, Object> getSessionStatus(String sessionId) {
        Long sId = Long.valueOf(sessionId);
        TrainingSession session = sessionMapper.selectById(sId);
        if (session == null) throw new RuntimeException("Session not found");

        Integer maxRounds = 10;
        if (session.getTemplateId() != null) {
            ScenarioTemplate tpl = templateMapper.selectById(session.getTemplateId());
            if (tpl != null && tpl.getDefaultRounds() != null) {
                maxRounds = tpl.getDefaultRounds();
            }
        }

        Map<String, Object> status = new HashMap<>();
        status.put("currentRound", session.getCurrentRound());
        status.put("totalRounds", maxRounds);
        status.put("status", session.getStatus());
        status.put("isEnd", "COMPLETED".equalsIgnoreCase(session.getStatus()));
        return status;
    }

    private String extractContent(String aiResponse) {
        try {
            if (JSONUtil.isTypeJSON(aiResponse)) {
                JSONObject json = JSONUtil.parseObj(aiResponse);
                if (json.containsKey("content")) return json.getStr("content");
            } else {
                int s = aiResponse.indexOf("{");
                int e = aiResponse.lastIndexOf("}");
                if (s >= 0 && e > s) {
                    String jsonStr = aiResponse.substring(s, e + 1);
                    JSONObject json = JSONUtil.parseObj(jsonStr);
                    if (json.containsKey("content")) return json.getStr("content");
                }
            }
        } catch (Exception ignored) {}
        return aiResponse;
    }

    @Override
    public List<SessionMessage> getDialogueHistory(String sessionId) {
        QueryWrapper<SessionMessage> query = new QueryWrapper<>();
        query.eq("session_id", Long.valueOf(sessionId)).orderByAsc("created_at");
        return messageMapper.selectList(query);
    }
}
