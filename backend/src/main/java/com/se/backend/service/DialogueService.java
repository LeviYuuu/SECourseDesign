package com.se.backend.service;

import com.se.backend.entity.SessionMessage;
import com.se.backend.entity.TrainingSession;
import com.se.backend.mapper.MessageMapper;
import com.se.backend.mapper.SessionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class DialogueService {
    @Autowired private MessageMapper messageMapper;
    @Autowired private SessionMapper sessionMapper;

    /**
     * 创建会话
     */
    public Long createSession(Long userId, Long templateId) {
        TrainingSession s = new TrainingSession();
        
        s.setUserId(userId);
        
        // ✅ 修复1：对应第2张报错图
        // 数据库字段是 template_id，所以实体类方法是 setTemplateId
        // 原先的 setScenarioId 已不存在
        s.setTemplateId(templateId); 
        
        s.setStatus("CREATED");
        s.setCurrentRound(0);
        s.setStartedAt(LocalDateTime.now());
        s.setCreatedAt(LocalDateTime.now());
        
        // ✅ 修复2：对应第3张报错图
        // 数据库 ID 是自增的 (AUTO_INCREMENT)，绝对不能手动设置 String 类型的 ID
        // s.setSessionId("sess_" + UUID...);  <-- 这行必须删掉！
        
        sessionMapper.insert(s);
        
        // 插入成功后，MyBatis 会自动把生成的 Long ID 填回对象
        return s.getSessionId(); 
    }

    /**
     * 处理对话
     */
    public Map<String, Object> nextRound(Long sessionId, Integer currentRound, String userAnswer) {
        // 1. 存用户消息
        SessionMessage userMsg = new SessionMessage();
        userMsg.setSessionId(sessionId);
        userMsg.setRoundNo(currentRound);
        userMsg.setSender("USER");
        userMsg.setContentText(userAnswer);
        userMsg.setCreatedAt(LocalDateTime.now());
        messageMapper.insert(userMsg);

        // 2. 更新会话轮次
        int nextRound = currentRound + 1;
        TrainingSession session = sessionMapper.selectById(sessionId);
        if(session != null) {
            session.setCurrentRound(nextRound);
            sessionMapper.updateById(session);
        }

        // 3. Mock AI 回复
        Map<String, Object> res = new HashMap<>();
        String aiQuestion;
        
        if (nextRound > 5) {
            res.put("isEnd", true);
            aiQuestion = "面试结束，请等待评估报告。";
        } else {
            res.put("isEnd", false);
            aiQuestion = "（AI追问）关于这一点，能展开说说具体的例子吗？";
        }

        // 存系统消息
        SessionMessage sysMsg = new SessionMessage();
        sysMsg.setSessionId(sessionId);
        sysMsg.setRoundNo(nextRound);
        sysMsg.setSender("SYSTEM");
        sysMsg.setContentText(aiQuestion);
        sysMsg.setCreatedAt(LocalDateTime.now());
        messageMapper.insert(sysMsg);

        res.put("round", nextRound);
        res.put("question", aiQuestion);
        return res;
    }
}
