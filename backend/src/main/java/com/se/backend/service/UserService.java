package com.se.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.se.backend.entity.User;
import com.se.backend.entity.UserCredential;
import com.se.backend.mapper.UserCredentialMapper;
import com.se.backend.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
public class UserService {
    @Autowired private UserMapper userMapper;
    @Autowired private UserCredentialMapper credentialMapper;

    /**
     * 登录逻辑：先查 User 表，再查 Credential 表
     */
    public User login(String accountNo, String password) {
        // 1. 根据 account_no 查询用户基础信息
        QueryWrapper<User> userQw = new QueryWrapper<>();
        userQw.eq("account_no", accountNo);
        // 过滤已软删除的用户
        userQw.isNull("deleted_at"); 
        
        User user = userMapper.selectOne(userQw);
        if (user == null) return null;

        // 2. 根据 user_id 查询凭证
        UserCredential cred = credentialMapper.selectById(user.getUserId());
        if (cred == null) return null;

        // 3. 验证密码 
        // 注意：实际生产中应使用 hash(password + salt)
        // 这里为了演示方便及兼容旧数据，暂用直接比对，您可随后改为 MD5/BCrypt
        if (!password.equals(cred.getPasswordHash())) {
            return null;
        }

        // 4. 更新最后登录时间
        cred.setLastLoginAt(LocalDateTime.now());
        credentialMapper.updateById(cred);

        return user;
    }

    /**
     * 注册逻辑：同时写入 User 和 Credential 表
     */
    @Transactional
    public boolean register(String accountNo, String password, String nickname) {
        // 1. 检查账号是否存在
        QueryWrapper<User> qw = new QueryWrapper<>();
        qw.eq("account_no", accountNo);
        if (userMapper.selectCount(qw) > 0) return false;

        // 2. 插入 User 表
        User user = new User();
        user.setAccountNo(accountNo);
        user.setAccountType("STUDENT"); // 默认类型
        user.setNickname(nickname);
        user.setStatus("ACTIVE");
        user.setCreatedAt(LocalDateTime.now());
        userMapper.insert(user);

        // 3. 插入 Credential 表
        UserCredential cred = new UserCredential();
        cred.setUserId(user.getUserId()); // 获取自增 ID
        cred.setPasswordHash(password); // 实际应存储 Hash
        cred.setPasswordSalt("mock_salt");
        credentialMapper.insert(cred);

        return true;
    }
}
