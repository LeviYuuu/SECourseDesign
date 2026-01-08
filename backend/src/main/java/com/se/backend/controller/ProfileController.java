package com.se.backend.controller;

import com.se.backend.common.Result;
import com.se.backend.service.ProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/profile")
public class ProfileController {
    @Autowired private ProfileService profileService;

    @GetMapping("/trend")
    public Result<Map<String, Object>> trend(@RequestParam Integer userId) {
        return Result.success(profileService.getTrend(userId));
    }

    @GetMapping("/history")
    public Result<Map<String, Object>> history(@RequestParam Integer userId) {
        return Result.success(profileService.getHistory(userId));
    }
}
