package com.se.backend.service;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.se.backend.dto.MessageRequest;
import com.se.backend.dto.MessageResponse;
import com.se.backend.entity.ScenarioTemplate;
import com.se.backend.entity.SessionMessage;
import com.se.backend.entity.TrainingSession;
import com.se.backend.mapper.ScenarioTemplateMapper;
import com.se.backend.mapper.SessionMessageMapper;
import com.se.backend.mapper.TrainingSessionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ChatService {

    @Autowired private TrainingSessionMapper sessionMapper;
    @Autowired private SessionMessageMapper messageMapper;
    @Autowired private ScenarioTemplateMapper templateMapper;
    @Autowired private LLMService llmService;

    @Transactional(rollbackFor = Exception.class)
    public MessageResponse processMessage(Long userId, MessageRequest req) {
        // 1. 基础校验
        TrainingSession session = sessionMapper.selectById(req.getSessionId());
        if (session == null) throw new RuntimeException("Session not found");
        // 权限校验可根据需求开启
        // if (!session.getUserId().equals(userId)) throw new RuntimeException("Unauthorized");

        if ("CREATED".equals(session.getStatus())) {
            session.setStatus("ONGOING");
            sessionMapper.updateById(session);
        }

        // 2. 用户消息入库
        SessionMessage userMsg = new SessionMessage();
        userMsg.setSessionId(session.getSessionId());
        userMsg.setRoundNo(session.getCurrentRound() + 1);
        userMsg.setSender("USER");
        userMsg.setInputMode(req.getInputMode());
        userMsg.setContentText(req.getContent());
        if ("VOICE".equals(req.getInputMode())) {
            userMsg.setAsrText(req.getAsrText());
            userMsg.setContentText(req.getAsrText());
        }
        userMsg.setCreatedAt(LocalDateTime.now());
        messageMapper.insert(userMsg);

        // 3. 准备 Persona (从快照或模板获取)
        String rolePersona = "严厉的面试官";
        try {
            boolean found = false;
            // 优先从 Session 快照取
            if (session.getScenarioSnapshotJson() != null) {
                JSONObject config = JSONUtil.parseObj(session.getScenarioSnapshotJson());
                if (config.containsKey("rolePersona")) {
                    rolePersona = config.getStr("rolePersona");
                    found = true;
                }
            }
            // 降级从模板表取
            if (!found && session.getTemplateId() != null) {
                ScenarioTemplate tpl = templateMapper.selectById(session.getTemplateId());
                if (tpl != null) rolePersona = tpl.getRolePersona();
            }
        } catch (Exception e) {
            System.err.println("Persona parse error: " + e.getMessage());
        }

        // ---【核心修复点：构建强指令 Prompt】---
        String systemPrompt = String.format(
            "【指令】\n" +
            "你现在的身份是：%s。\n" +
            "背景：正在与用户进行高压场景模拟（如面试、答辩）。\n" +
            "要求：\n" +
            "1. 请完全沉浸在角色中，严禁自称AI或语言模型，严禁说'我可以帮你...'等服务型话语。\n" +
            "2. 不需要打招呼，直接针对用户的回答进行犀利的追问、质疑或评价。\n" +
            "3. 保持简短、口语化，像真人一样对话。", 
            rolePersona
        );

        // 4. 获取历史记录
        LambdaQueryWrapper<SessionMessage> query = new LambdaQueryWrapper<>();
        query.eq(SessionMessage::getSessionId, session.getSessionId())
             .orderByAsc(SessionMessage::getMessageId)
             .last("LIMIT 20");
        List<SessionMessage> history = messageMapper.selectList(query);

        // 5. 调用 LLM (传入构建好的 Prompt，而不是原本的单词)
        String aiReplyText = llmService.callAI(history, systemPrompt);

        // 6. 系统消息入库
        SessionMessage sysMsg = new SessionMessage();
        sysMsg.setSessionId(session.getSessionId());
        sysMsg.setRoundNo(session.getCurrentRound() + 1);
        sysMsg.setSender("SYSTEM");
        sysMsg.setContentText(aiReplyText);
        sysMsg.setGenSource("LLM");
        sysMsg.setCreatedAt(LocalDateTime.now());
        messageMapper.insert(sysMsg);

        // 7. 更新 Session
        session.setCurrentRound(session.getCurrentRound() + 1);
        session.setUpdatedAt(LocalDateTime.now());
        sessionMapper.updateById(session);

        // 8. 返回
        MessageResponse resp = new MessageResponse();
        resp.setMessageId(sysMsg.getMessageId());
        resp.setSender("SYSTEM");
        resp.setContent(aiReplyText);
        resp.setRoundNo(sysMsg.getRoundNo());
        resp.setCreatedAt(sysMsg.getCreatedAt());
        
        return resp;
    }
}
