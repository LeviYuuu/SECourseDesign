package com.se.backend.controller;

import com.se.backend.common.Result;
import com.se.backend.dto.UserProfileResponse;
import com.se.backend.service.ProfileService;
import com.se.backend.util.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class ProfileController {

    @Autowired
    private ProfileService profileService;

    @GetMapping("/profile")
    public Result<UserProfileResponse> getProfile() {
        Long userId = UserContext.getUserId(); // 从 Token 获取
        UserProfileResponse resp = profileService.getUserProfile(userId);
        return Result.success(resp);
    }
}
