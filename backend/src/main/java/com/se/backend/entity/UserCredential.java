package com.se.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("user_credentials")
public class UserCredential {
    @TableId(value = "user_id", type = IdType.INPUT) // 与 User 表 ID 一致
    private Long userId;

    @TableField("password_hash")
    private String passwordHash;

    @TableField("password_salt")
    private String passwordSalt;

    @TableField("last_login_at")
    private LocalDateTime lastLoginAt;
}
