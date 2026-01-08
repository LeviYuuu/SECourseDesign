package com.se.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("users")
public class User {
    @TableId(value = "user_id", type = IdType.AUTO)
    private Long userId;

    @TableField("account_no")
    private String accountNo; // 对应数据库 account_no

    @TableField("account_type")
    private String accountType; // 对应数据库 account_type

    private String nickname;
    private String status;

    @TableField("created_at")
    private LocalDateTime createdAt;
    
    @TableField("deleted_at")
    private LocalDateTime deletedAt;
}
