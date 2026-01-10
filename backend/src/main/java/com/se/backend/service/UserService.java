package com.se.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.se.backend.dto.LoginResponse;
import com.se.backend.dto.UserRegisterRequest;
import com.se.backend.entity.User;
import com.se.backend.entity.UserCredentials;
import com.se.backend.mapper.UserCredentialsMapper;
import com.se.backend.mapper.UserMapper;
import com.se.backend.util.JwtUtil;
import com.se.backend.util.PasswordUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserCredentialsMapper credentialsMapper;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 用户注册
     */
    @Transactional(rollbackFor = Exception.class)
    public Long register(UserRegisterRequest req) {
        // 1. 确定账号类型 (优先使用 Email)
        String type = "EMAIL";
        String accountNo = req.getEmail();
        
        if (req.getPhone() != null && !req.getPhone().isEmpty()) {
            type = "PHONE";
            accountNo = req.getPhone();
        }
        
        if (accountNo == null || accountNo.isEmpty()) {
            throw new IllegalArgumentException("账号（邮箱或手机）不能为空");
        }

        // 2. 查重
        Long count = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getAccountType, type)
                .eq(User::getAccountNo, accountNo));
        
        if (count > 0) {
            throw new RuntimeException("该账号已被注册");
        }

        // 3. 插入用户基础信息
        User user = new User();
        user.setNickname(req.getNickname());
        user.setAccountType(type);
        user.setAccountNo(accountNo);
        user.setStatus("ACTIVE");
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        
        userMapper.insert(user); // MyBatis-Plus 会自动回填 user_id

        // 4. 处理密码 (加盐哈希)
        String salt = PasswordUtil.generateSalt();
        String hash = PasswordUtil.hashPassword(req.getPassword(), salt);

        UserCredentials cred = new UserCredentials();
        cred.setUserId(user.getUserId());
        cred.setPasswordSalt(salt);
        cred.setPasswordHash(hash);
        
        credentialsMapper.insert(cred);

        return user.getUserId();
    }

    /**
     * 用户登录
     */
    public LoginResponse login(String accountNo, String password) {
        // 1. 查找用户
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getAccountNo, accountNo));
        
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 2. 获取凭证
        UserCredentials cred = credentialsMapper.selectById(user.getUserId());
        if (cred == null) {
            throw new RuntimeException("账号数据异常");
        }

        // 3. 校验密码
        if (!PasswordUtil.verify(password, cred.getPasswordSalt(), cred.getPasswordHash())) {
            throw new RuntimeException("密码错误");
        }

        // 4. 更新登录时间
        cred.setLastLoginAt(LocalDateTime.now());
        credentialsMapper.updateById(cred);

        // 5. 生成 Token
        String token = jwtUtil.createToken(user.getUserId(), user.getAccountNo());

        // 6. 构建返回对象
        LoginResponse resp = new LoginResponse();
        resp.setUserId(user.getUserId());
        resp.setNickname(user.getNickname());
        resp.setToken(token);
        resp.setExpiresIn(86400L); // 24小时

        return resp;
    }
}
