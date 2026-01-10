package com.se.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("user_credentials")
public class UserCredentials {
    // 这里的 userId 既是主键也是外键，不能自增，需手动输入或跟 User 保持一致
    @TableId(value = "user_id", type = IdType.INPUT)
    private Long userId;

    @TableField("password_hash")
    private String passwordHash;

    @TableField("password_salt")
    private String passwordSalt;

    @TableField("last_login_at")
    private LocalDateTime lastLoginAt;
}
