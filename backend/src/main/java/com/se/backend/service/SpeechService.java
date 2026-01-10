package com.se.backend.service;

import org.springframework.web.multipart.MultipartFile;
import java.util.Map;

/**
 * 语音服务接口
 * 严格对齐详细设计规约 4.3 章节
 */
public interface SpeechService {

    /**
     * 上传音频文件
     * 依据文档 4.3.3 (1): uploadAudio(MultipartFile audioFile, String sessionId, Integer userId)
     */
    Map<String, Object> uploadAudio(MultipartFile audioFile, String sessionId, Integer userId);

    /**
     * 语音识别
     * 依据文档 4.3.3 (2): transcribeAudio(Integer audioId)
     */
    Map<String, Object> transcribeAudio(Integer audioId);

    /**
     * 文字转语音 (TTS)
     */
    String generateTTS(String text, String voice, Float speed);
}
