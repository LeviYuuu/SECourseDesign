package com.se.backend.dto;
import lombok.Data;

@Data
public class UserProfileResponse {
    private Long userId;
    private String nickname;
    private String email; // 或 accountNo
    private Integer totalSessions; // 总训练次数
    private Integer totalDurationMinutes; // 练习总时长(模拟)
}
