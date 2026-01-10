package com.se.backend.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.se.backend.config.AiConfig;
import com.se.backend.entity.SessionMessage;
import com.se.backend.service.LLMService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * OpenAI 兼容 LLM 实现：/chat/completions
 * 对接：Aizex / OpenAI
 */
@Service
public class OpenAiLLMServiceImpl implements LLMService {

    @Autowired
    private AiConfig aiConfig;

    @Override
    public String callAI(List<SessionMessage> history, String systemPrompt) {
        // [修复点 1] 获取嵌套的 LlmConfig
        AiConfig.LlmConfig llmConfig = aiConfig.getLlm();

        // 校验配置是否存在
        if (llmConfig == null) {
            throw new IllegalStateException("LLM 配置未找到，请检查 application.yml 中是否配置了 ai.llm");
        }

        // [修复点 2] 从 llmConfig 中获取参数
        String baseUrl = llmConfig.getBaseUrl();
        String apiKey = llmConfig.getApiKey();
        String model = llmConfig.getModel();

        if (StrUtil.isBlank(baseUrl) || StrUtil.isBlank(apiKey) || StrUtil.isBlank(model)) {
            throw new IllegalStateException("LLM 配置不完整：请配置 ai.llm.base-url / api-key / model");
        }

        String url = joinUrl(baseUrl, "/chat/completions");

        // 若 Persona/提示词要求结构化 JSON 输出，可配合 response_format
        boolean expectJson = containsJsonRequirement(systemPrompt);

        JSONArray messages = new JSONArray();

        // 1) system prompt
        String effectiveSystemPrompt = StrUtil.nullToEmpty(systemPrompt).trim();
        if (StrUtil.isNotBlank(effectiveSystemPrompt)) {
            JSONObject sys = new JSONObject();
            sys.put("role", "system");
            sys.put("content", effectiveSystemPrompt);
            messages.add(sys);
        }

        // 2) history
        if (history != null) {
            for (SessionMessage msg : history) {
                if (msg == null) continue;
                String content = msg.getContentText();
                if (StrUtil.isBlank(content)) continue;

                String role = "assistant";
                if ("USER".equalsIgnoreCase(msg.getSender())) role = "user";
                if ("SYSTEM".equalsIgnoreCase(msg.getSender())) role = "assistant";

                JSONObject m = new JSONObject();
                m.put("role", role);
                m.put("content", content);
                messages.add(m);
            }
        }

        JSONObject requestBody = new JSONObject();
        requestBody.put("model", model);
        requestBody.put("messages", messages);
        requestBody.put("temperature", 0.8);

        if (expectJson) {
            JSONObject responseFormat = new JSONObject();
            responseFormat.put("type", "json_object");
            requestBody.put("response_format", responseFormat);
        }

        // 发送请求
        try {
            HttpResponse resp = HttpRequest.post(url)
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .body(requestBody.toString())
                    .timeout(120_000) // 2分钟超时
                    .execute();

            if (resp.getStatus() < 200 || resp.getStatus() >= 300) {
                throw new IllegalStateException("LLM 调用失败，HTTP=" + resp.getStatus() + ", body=" + resp.body());
            }

            return extractAssistantContent(resp.body());
        } catch (Exception e) {
            throw new IllegalStateException("LLM 网络请求异常: " + e.getMessage(), e);
        }
    }

    private static String extractAssistantContent(String body) {
        if (StrUtil.isBlank(body)) return "";

        JSONObject json = JSONUtil.parseObj(body);
        // 增加容错判断
        if (!json.containsKey("choices")) return "";
        
        JSONArray choices = json.getJSONArray("choices");
        if (choices == null || choices.isEmpty()) return "";

        JSONObject first = choices.getJSONObject(0);
        if (first == null) return "";

        JSONObject message = first.getJSONObject("message");
        if (message == null) return "";

        String content = message.getStr("content");
        return StrUtil.nullToEmpty(content).trim();
    }

    private static boolean containsJsonRequirement(String systemPrompt) {
        if (StrUtil.isBlank(systemPrompt)) return false;
        String p = systemPrompt.toLowerCase();
        return p.contains("json") || p.contains("json_object") || p.contains("response_format");
    }

    private static String joinUrl(String baseUrl, String path) {
        String b = baseUrl.trim();
        if (b.endsWith("/")) b = b.substring(0, b.length() - 1);
        String p = path.startsWith("/") ? path : ("/" + path);
        return b + p;
    }
}
