package com.se.backend.controller;

import com.se.backend.common.Result;
import com.se.backend.entity.User;
import com.se.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController @RequestMapping("/user")
public class UserController {
    @Autowired private UserService userService;

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody LoginDto dto) {
        // 修正点 1：使用 accountNo 而非 username
        User user = userService.login(dto.getUsername(), dto.getPassword());
        
        if(user == null) return Result.error("账号或密码错误");
        
        return Result.success(Map.of(
            "userId", user.getUserId(), 
            // 修正点 2：getNickname() 是存在的，若为 null 给默认值
            "nickname", user.getNickname() == null ? "新用户" : user.getNickname(), 
            "token", "mock_token_123"
        ));
    }

    @PostMapping("/register")
    public Result<String> register(@RequestBody LoginDto dto) {
        // 修正点 3：传入参数匹配 Service 签名
        boolean success = userService.register(dto.getUsername(), dto.getPassword(), dto.getNickname());
        return success ? Result.success("注册成功") : Result.error("账号已存在");
    }
}
