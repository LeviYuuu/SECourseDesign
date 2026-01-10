package com.se.backend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * ASR 配置（语音识别）。
 */
@Data
@Component
@ConfigurationProperties(prefix = "ai.asr")
public class AsrConfig {

    /**
     * ASR API 基础地址（OpenAI 兼容通常为 .../v1）
     * 若不配置，将尝试复用 ai.llm.baseUrl
     */
    private String baseUrl;

    /**
     * ASR API Key
     * 若不配置，将尝试复用 ai.llm.apiKey
     */
    private String apiKey;

    /**
     * ASR 模型名称（例如 whisper-1）
     */
    private String model;

    /**
     * 语言（可选），例如 zh
     */
    private String language;
}
