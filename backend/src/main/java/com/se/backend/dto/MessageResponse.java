package com.se.backend.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MessageResponse {
    private Long messageId;
    private String sender; // SYSTEM
    private String content;
    private Integer roundNo;
    private String audioUri; // 如果开启了 TTS
    private LocalDateTime createdAt;
}
