package com.se.backend.dto;

import lombok.Data;

@Data
public class UserRegisterRequest {
    private String email;
    private String phone;
    private String password;
    private String nickname;
}
