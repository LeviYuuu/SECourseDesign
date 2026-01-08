package com.se.backend.controller;
import lombok.Data;

@Data
public class LoginDto {
    private String username; // 前端传参名
    private String password;
    private String nickname; // 注册用
}
