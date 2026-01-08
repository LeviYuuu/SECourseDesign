package com.se.backend.controller;

import com.se.backend.common.Result;
import com.se.backend.entity.User;
import com.se.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/auth") // 根据规约，通常认证相关接口放在 /auth 或 /user 下，此处对应 AuthController
public class AuthController {

    @Autowired private UserService userService;

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody LoginDto dto) {
        // 修正点：将 DTO 中的 username 映射为 Service 需要的 accountNo
        User user = userService.login(dto.getUsername(), dto.getPassword());
        
        if(user == null) return Result.error("账号或密码错误");
        
        return Result.success(Map.of(
            "userId", user.getUserId(), 
            // 修正点：使用 getNickname()，并做空指针保护
            "nickname", user.getNickname() == null ? user.getAccountNo() : user.getNickname(), 
            "token", "mock_token_123_xyz"
        ));
    }

    @PostMapping("/register")
    public Result<String> register(@RequestBody LoginDto dto) {
        // 修正点：参数传递匹配 Service 新签名
        boolean success = userService.register(dto.getUsername(), dto.getPassword(), dto.getNickname());
        return success ? Result.success("注册成功") : Result.error("账号已存在");
    }
}
