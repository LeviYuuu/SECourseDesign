package com.se.backend.dto;

import lombok.Data;

@Data
public class UserLoginRequest {
    // 对应 account_no，前端可能传 邮箱/手机号/学号
    private String username; 
    private String password;
}
