package com.se.backend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 全局 AI 配置类
 * 对应 YAML 根节点: ai
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "ai")
public class AiConfig {
    
    // Aizex / OpenAI LLM 配置
    private LlmConfig llm;
    
    // 语音服务配置
    private SpeechConfig speech;

    /**
     * LLM 配置部分
     * 对应: ai.llm
     */
    @Data
    public static class LlmConfig {
        private String baseUrl;
        private String apiKey;
        private String model;
    }

    /**
     * 语音服务配置部分
     * 对应: ai.speech
     */
    @Data
    public static class SpeechConfig {
        private VolcengineConfig volcengine;
    }

    /**
     * 火山引擎配置 (迁移自之前的 AiConfig 根属性)
     * 对应: ai.speech.volcengine
     */
    @Data
    public static class VolcengineConfig {
        private String appId;
        private String accessToken;
        private AsrConfig asr;
        private TtsConfig tts;
    }

    @Data
    public static class AsrConfig {
        private String submitUrl;
        private String queryUrl;
        private String resourceId;
        private DefaultConfig defaultConfig;
    }

    @Data
    public static class DefaultConfig {
        private String modelName;
        private String format;
        private Integer rate;
        private Integer bits;
        private Integer channel;
    }

    @Data
    public static class TtsConfig {
        private String wsUrl;
        private String resourceId;
        private String voiceType;
        private String format;
        private Double speed;
    }
}
