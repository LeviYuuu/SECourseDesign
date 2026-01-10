package com.se.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Data
@TableName("session_messages")
public class SessionMessage {
    /**
     * 消息ID
     */
    @TableId(value = "message_id", type = IdType.AUTO)
    private Long messageId;

    /**
     * 所属会话ID
     */
    @TableField("session_id")
    private Long sessionId;

    /**
     * 轮次编号
     */
    @TableField("round_no")
    private Integer roundNo;

    /**
     * 发送方 (USER/SYSTEM)
     */
    private String sender; 

    /**
     * 阶段标记 (如: OPENING, PROBING) - 对应 DB: stage
     */
    private String stage;

    /**
     * 展示文本
     */
    @TableField("content_text")
    private String contentText;

    /**
     * 输入模式 (TEXT/VOICE) - 对应 DB: input_mode
     */
    @TableField("input_mode")
    private String inputMode;

    /**
     * 音频地址 - 对应 DB: audio_uri
     */
    @TableField("audio_uri")
    private String audioUri;

    /**
     * ASR识别文本 - 对应 DB: asr_text
     */
    @TableField("asr_text")
    private String asrText;

    /**
     * ASR置信度 - 对应 DB: asr_confidence
     */
    @TableField("asr_confidence")
    private BigDecimal asrConfidence;

    /**
     * 用户校正文本 - 对应 DB: corrected_text
     */
    @TableField("corrected_text")
    private String correctedText;

    /**
     * 系统生成来源 (LLM/FixedBank) - 对应 DB: gen_source
     * ★ 修复报错的关键字段 ★
     */
    @TableField("gen_source")
    private String genSource;

    /**
     * LLM链路追踪ID - 对应 DB: llm_trace_id
     */
    @TableField("llm_trace_id")
    private String llmTraceId;

    /**
     * 是否命中要点 - 对应 DB: relevance_hit
     */
    @TableField("relevance_hit")
    private Boolean relevanceHit;

    /**
     * 创建时间
     */
    @TableField("created_at")
    private LocalDateTime createdAt;
}
