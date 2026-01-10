package com.se.backend.controller;

import com.se.backend.common.Result;
import com.se.backend.dto.*;
import com.se.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public Result<Map<String, Long>> register(@RequestBody UserRegisterRequest req) {
        Long userId = userService.register(req);
        
        // 严格对齐：返回对象结构 { "userId": 1001 }
        Map<String, Long> data = new HashMap<>();
        data.put("userId", userId);
        
        return Result.success(data);
    }

    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody UserLoginRequest req) {
        LoginResponse resp = userService.login(req.getUsername(), req.getPassword());
        return Result.success(resp);
    }
}
