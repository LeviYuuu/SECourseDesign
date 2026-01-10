package com.se.backend.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class LoginResponse {
    private String token;
    private long expiresIn; // 秒
    private String nickname;
    private Long userId;
}