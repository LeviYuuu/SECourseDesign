package com.se.backend.service;

import com.se.backend.entity.SessionMessage;

import java.util.List;

/**
 * LLM Service abstraction.
 *
 * 当前项目调用 OpenAI 兼容的 /chat/completions 接口；
 * 具体实现见: com.se.backend.service.impl.OpenAiLLMServiceImpl
 */
public interface LLMService {

    /**
     * 调用 LLM 生成回复。
     *
     * @param history      会话历史（按时间升序）
     * @param systemPrompt System Prompt / Persona（可为空）
     * @return LLM 返回的 assistant 内容（可能是 JSON 字符串，或纯文本）
     */
    String callAI(List<SessionMessage> history, String systemPrompt);
}
