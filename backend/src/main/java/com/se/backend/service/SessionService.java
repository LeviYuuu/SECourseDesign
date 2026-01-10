package com.se.backend.service;

import cn.hutool.json.JSONUtil;
import com.se.backend.dto.SessionCreateRequest;
import com.se.backend.entity.ScenarioTemplate;
import com.se.backend.entity.TrainingSession;
import com.se.backend.mapper.ScenarioTemplateMapper;
import com.se.backend.mapper.TrainingSessionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class SessionService {

    @Autowired
    private TrainingSessionMapper sessionMapper;

    @Autowired
    private ScenarioTemplateMapper templateMapper;

    /**
     * 创建会话
     * 对应设计规约 4.2.3 (2)
     */
    public TrainingSession createSession(Long userId, SessionCreateRequest req) {
        // 1. 获取模板ID (兼容 scenarioId 参数)
        Long tplId = req.getTemplateId();
        
        // 2. 查模板
        ScenarioTemplate tpl = null;
        if (tplId != null) {
            tpl = templateMapper.selectById(tplId);
        }

        // 3. 构建会话实体
        TrainingSession session = new TrainingSession();
        
        // 【核心修复】设置 UserID (解决 Field 'user_id' doesn't have a default value)
        session.setUserId(userId); 
        
        session.setTemplateId(tplId);
        session.setStatus("CREATED");
        session.setCurrentRound(0);
        session.setTtsEnabled(Boolean.TRUE.equals(req.getTtsEnabled()));

        // 【时间戳修复】同时设置 create/start/update，防止数据库非空约束报错
        LocalDateTime now = LocalDateTime.now();
        session.setStartedAt(now);
        session.setCreatedAt(now);
        session.setUpdatedAt(now); 

        // 4. 保存快照
        if (tpl != null && tpl.getConfigJson() != null) {
            session.setScenarioSnapshotJson(tpl.getConfigJson());
        } else {
            // 默认配置兜底
            session.setScenarioSnapshotJson("{\"rolePersona\":\"面试官\",\"difficulty\":\"L2\"}");
        }

        // 5. 落库
        sessionMapper.insert(session);
        
        return session;
    }
}
