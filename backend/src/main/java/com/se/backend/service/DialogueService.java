package com.se.backend.service;

import com.se.backend.entity.SessionMessage;
import java.util.List;
import java.util.Map;

public interface DialogueService {
    /**
     * 生成 AI 提问/回答
     * 修改返回类型为 Map，以便返回 isEnd, totalRounds 等控制字段
     */
    Map<String, Object> generateQuestion(String sessionId, Integer currentRound, List<SessionMessage> history);

    List<SessionMessage> getDialogueHistory(String sessionId);
}
