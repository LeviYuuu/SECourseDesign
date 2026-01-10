package com.se.backend.dto;

import lombok.Data;

@Data
public class MessageRequest {
    private Long sessionId;
    
    // 输入模式: TEXT / VOICE
    private String inputMode; 
    
    // 如果是 TEXT
    private String content; 
    
    // 如果是 VOICE (通常是上传文件后的路径，或是Base64，这里假设前端先传文件拿到 audioUri)
    private String audioUri; 
    
    // 前端若有预处理的ASR结果可传入，否则由后端处理
    private String asrText; 
}
