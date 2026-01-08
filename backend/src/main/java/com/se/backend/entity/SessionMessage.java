package com.se.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("session_messages")
public class SessionMessage {
    @TableId(value = "message_id", type = IdType.AUTO)
    private Long messageId;

    @TableField("session_id")
    private Long sessionId;

    @TableField("round_no")
    private Integer roundNo;

    private String sender; // 'USER' or 'SYSTEM'
    
    @TableField("content_text")
    private String contentText;

    @TableField("created_at")
    private LocalDateTime createdAt;
}
