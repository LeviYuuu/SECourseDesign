package com.se.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("audio_objects") // 假设表名为 audio_objects
public class AudioObject {
    @TableId(type = IdType.AUTO)
    private Integer audioId;
    private String sessionId;
    private Integer userId;
    private String audioUrl; // OSS 存储路径
    private Long duration;   // 时长(秒)
    private Long size;       // 文件大小(字节)
    private LocalDateTime createdAt;
}
